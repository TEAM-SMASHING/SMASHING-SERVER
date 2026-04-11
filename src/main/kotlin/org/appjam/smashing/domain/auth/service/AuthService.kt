package org.appjam.smashing.domain.auth.service

import org.appjam.smashing.domain.auth.dto.command.SignInRequestCommand
import org.appjam.smashing.domain.auth.dto.command.SignUpRequestCommand
import org.appjam.smashing.domain.auth.dto.command.TokenReissueCommand
import org.appjam.smashing.domain.auth.dto.response.SignInResponse
import org.appjam.smashing.domain.auth.dto.response.SignUpResponse
import org.appjam.smashing.domain.auth.dto.response.TokenReissueResponse
import org.appjam.smashing.domain.auth.social.SocialAuthServiceManager
import org.appjam.smashing.domain.sport.repository.SportRepository
import org.appjam.smashing.domain.tier.repository.TierRepository
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.appjam.smashing.domain.user.repository.UserRepository
import org.appjam.smashing.domain.user.repository.UserSportProfileRepository
import org.appjam.smashing.domain.user.service.UserService.Companion.DISTRICT_SUFFIX
import org.appjam.smashing.domain.user.service.UserService.Companion.OPEN_CHAT_URL_REGEX
import org.appjam.smashing.global.auth.jwt.components.JwtProvider
import org.appjam.smashing.global.auth.jwt.components.JwtValidator
import org.appjam.smashing.global.auth.jwt.dto.TokenDto
import org.appjam.smashing.global.auth.jwt.filter.JwtBlacklistManager
import org.appjam.smashing.global.auth.jwt.filter.JwtRefreshStore
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val socialAuthServiceManager: SocialAuthServiceManager,
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
    private val sportRepository: SportRepository,
    private val tierRepository: TierRepository,
    private val userSportProfileRepository: UserSportProfileRepository,
    private val jwtRefreshStore: JwtRefreshStore,
    private val jwtBlacklistManager: JwtBlacklistManager,
    private val jwtValidator: JwtValidator,
) {
    @Transactional
    fun signIn(
        requestCommand: SignInRequestCommand,
    ): SignInResponse {
        val (provider, socialId) = socialAuthServiceManager.getSocialId(requestCommand)

        val user = userRepository.findBySocialIdAndProvider(
            socialId = socialId,
            provider = provider,
        ) ?: return SignInResponse.from(socialId = socialId)

        val userId = user.id ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val token = issueAndStoreTokens(userId)

        return SignInResponse.from(
            accessToken = token.accessToken.token,
            refreshToken = token.refreshToken.token,
            socialId = socialId,
            userId = userId,
            nickname = user.nickname,
        )
    }

    @Transactional
    fun signUp(
        requestCommand: SignUpRequestCommand,
    ): SignUpResponse {
        validateUser(requestCommand)

        val sport = sportRepository.findByCode(requestCommand.sportCode)
            ?: throw CustomException(ErrorCode.SPORT_NOT_FOUND)

        val initLp = requestCommand.experienceRange.initLp
        val initTier = tierRepository.findBySportIdAndLpInRange(
            sportId = sport.id!!,
            lp = initLp,
        ) ?: throw CustomException(ErrorCode.INVALID_INITIAL_TIER)

        validateCommand(requestCommand)

        val user = userRepository.save(
            User.create(
                socialId = requestCommand.socialId,
                provider = requestCommand.provider,
                nickname = requestCommand.nickname,
                gender = requestCommand.gender,
                openchatUrl = requestCommand.openChatUrl.trim(),
                region = requestCommand.region.trim(),
            )
        )

        val profile = userSportProfileRepository.save(
            UserSportProfile.create(
                lp = initLp,
                user = user,
                sport = sport,
                tier = initTier,
            )
        )

        // 처음 가입한 프로필로 활성 프로필 업데이트
        user.updateActiveProfile(profileId = profile.id!!)

        val token = issueAndStoreTokens(user.id!!)

        return SignUpResponse.from(
            accessToken = token.accessToken.token,
            refreshToken = token.refreshToken.token,
            userId = user.id,
            nickname = user.nickname,
        )
    }

    @Transactional
    fun logout(
        accessToken: String,
        userId: String,
    ) {
        // 유저 검증 - 토큰 subject 확인 및 유저 조회 (없을 경우 예외 발생)
        val token = jwtProvider.validateAndExtractAccessToken(accessToken, userId)

        jwtRefreshStore.deleteAllForUser(userId)

        jwtBlacklistManager.add(token)
    }

    @Transactional
    fun tokenReissue(
        reqeustCommand: TokenReissueCommand,
    ): TokenReissueResponse {
        val token = reqeustCommand.refreshToken

        // 토큰 검증
        jwtValidator.verifyToken(token)

        // 토큰이 기존에 존재하지 않을 경우 예외 발생
        if (!jwtRefreshStore.exists(token)) {
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        // 유저 조회
        val userId = jwtProvider.extractRefreshSubject(token)

        // redis 저장소에서 token 삭제
        jwtRefreshStore.deleteToken(token)

        val newToken = issueAndStoreTokens(userId)

        return TokenReissueResponse.from(
            accessToken = newToken.accessToken.token,
            refreshToken = newToken.refreshToken.token,
        )
    }

    @Transactional
    fun withdraw(
        accessToken: String,
        userId: String,
    ) {
        // 유저 검증 - 토큰 subject 확인 및 유저 조회 (없을 경우 예외 발생)
        val token = jwtProvider.validateAndExtractAccessToken(accessToken, userId)

        val user = userRepository.findByIdOrNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        // 회원 정보 삭제
        userRepository.delete(user)

        // 토큰 무효화
        jwtBlacklistManager.add(token)

        jwtRefreshStore.deleteAllForUser(userId)
    }

    private fun validateUser(requestCommand: SignUpRequestCommand) {
        if (userRepository.existsBySocialId(requestCommand.socialId)) {
            throw CustomException(ErrorCode.DUPLICATE_KAKAO_ID)
        }

        if (userRepository.existsByNickname(requestCommand.nickname)) {
            throw CustomException(ErrorCode.DUPLICATE_NICKNAME)
        }
    }

    private fun validateCommand(requestCommand: SignUpRequestCommand) {
        val trimmedUrl = requestCommand.openChatUrl.trim()
        if (!OPEN_CHAT_URL_REGEX.matches(trimmedUrl)) {
            throw CustomException(ErrorCode.INVALID_OPENCHAT_FORMAT)
        }

        val trimmedRegion = requestCommand.region.trim()
        if (!trimmedRegion.endsWith(DISTRICT_SUFFIX)) {
            throw CustomException(ErrorCode.INVALID_REGION)
        }
    }

    private fun issueAndStoreTokens(userId: String): TokenDto {
        val token = jwtProvider.issueToken(userId)

        jwtRefreshStore.save(
            userId = userId,
            refreshToken = token.refreshToken.token,
            ttlMillis = jwtProvider.getRefreshTtlMillis(token.refreshToken.token)
        )

        return token
    }
}
