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
            ?: return SignInResponse(
                accessToken = null,
                refreshToken = null,
                kakaoId = kakaoId,
                userId = null,
                nickname = null,
            )

        val userId = user.id ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val token = jwtProvider.issueToken(userId)
        val accessToken = token.accessToken.token
        val refreshToken = token.refreshToken.token

        jwtRefreshStore.save(
            userId = userId,
            refreshToken = refreshToken,
            ttlMillis = jwtProvider.getRefreshTtlMillis(refreshToken)
        )

        return SignInResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
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

        val trimmedUrl = requestCommand.openChatUrl.trim()
        validateOpenChatUrl(trimmedUrl)

        val trimmedRegion = requestCommand.region.trim()
        validateRegion(trimmedRegion)

        val user = userRepository.save(
            User.create(
                kakaoId = requestCommand.kakaoId,
                nickname = requestCommand.nickname,
                gender = requestCommand.gender,
                openchatUrl = trimmedUrl,
                region = trimmedRegion,
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

        user.updateActiveProfile(profileId = profile.id!!)

        val token = jwtProvider.issueToken(user.id!!)
        val accessToken = token.accessToken.token
        val refreshToken = token.refreshToken.token

        jwtRefreshStore.save(
            userId = user.id!!,
            refreshToken = refreshToken,
            ttlMillis = jwtProvider.getRefreshTtlMillis(refreshToken)
        )

        return SignUpResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = user.id,
            nickname = user.nickname,
        )
    }

    private fun validateUser(requestCommand: SignUpRequestCommand) {
        if (userRepository.existsByKakaoId(requestCommand.kakaoId)) {
            throw CustomException(ErrorCode.DUPLICATE_KAKAO_ID)
        }

        if (userRepository.existsByNickname(requestCommand.nickname)) {
            throw CustomException(ErrorCode.DUPLICATE_NICKNAME)
        }
    }

    private fun validateOpenChatUrl(trimmedUrl: String) {
        if (!OPEN_CHAT_URL_REGEX.matches(trimmedUrl)) {
            throw CustomException(ErrorCode.INVALID_OPENCHAT_FORMAT)
        }
    }

    private fun validateRegion(region: String) {
        if (!region.endsWith(DISTRICT_SUFFIX)) {
            throw CustomException(ErrorCode.INVALID_REGION)
        }
    }

    @Transactional
    fun logout(
        accessToken: String,
        userId: String,
    ) {
        jwtRefreshStore.deleteAllForUser(userId)

        jwtBlacklistManager.add(accessToken)
    }
}
