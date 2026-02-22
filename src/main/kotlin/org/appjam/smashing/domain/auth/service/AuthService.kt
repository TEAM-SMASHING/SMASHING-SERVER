package org.appjam.smashing.domain.auth.service

import org.appjam.smashing.domain.auth.dto.command.SignInRequestCommand
import org.appjam.smashing.domain.auth.dto.command.SignUpRequestCommand
import org.appjam.smashing.domain.auth.dto.response.SignInResponse
import org.appjam.smashing.domain.auth.dto.response.SignUpResponse
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
import org.appjam.smashing.global.auth.jwt.dto.TokenDto
import org.appjam.smashing.global.auth.jwt.filter.JwtBlacklistManager
import org.appjam.smashing.global.auth.jwt.filter.JwtRefreshStore
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
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
) {
    @Transactional
    fun signIn(requestCommand: SignInRequestCommand): SignInResponse {
        val kakaoId = socialAuthServiceManager.getKakaoId(requestCommand.accessToken)

        val user = userRepository.findByKakaoId(kakaoId)
            ?: return SignInResponse.from(
                kakaoId = kakaoId,
            )

        val userId = user.id ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val token = issueAndStoreTokens(userId)

        return SignInResponse.from(
            accessToken = token.accessToken.token,
            refreshToken = token.refreshToken.token,
            kakaoId = kakaoId,
            userId = userId,
            nickname = user.nickname,
        )
    }

    @Transactional
    fun signUp(requestCommand: SignUpRequestCommand): SignUpResponse {
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
                kakaoId = requestCommand.kakaoId,
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
        validateAccessTokenSubject(
            accessToken = accessToken,
            userId = userId,
        )

        // 유저에게 저장된 모든 리프레시 토큰을 삭제
        jwtRefreshStore.deleteAllForUser(userId)

        // 블랙리스트에 엑세스 토큰을 추가하여 토큰 무효화
        jwtBlacklistManager.add(accessToken)
    }

    private fun validateUser(requestCommand: SignUpRequestCommand) {
        if (userRepository.existsByKakaoId(requestCommand.kakaoId)) {
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

        // Redis에 리프레시 토큰 저장
        jwtRefreshStore.save(
            userId = userId,
            refreshToken = token.refreshToken.token,
            ttlMillis = jwtProvider.getRefreshTtlMillis(token.refreshToken.token)
        )

        return token
    }

    private fun validateAccessTokenSubject(
        accessToken: String,
        userId: String,
    ) {
        val subject = jwtProvider.extractSubject(accessToken)
        if (subject != userId) {
            throw CustomException(ErrorCode.ACCESS_TOKEN_SUBJECT_MISMATCH)
        }
    }
}
