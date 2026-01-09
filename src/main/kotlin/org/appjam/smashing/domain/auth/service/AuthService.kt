package org.appjam.smashing.domain.auth.service

import org.appjam.smashing.domain.auth.command.SignInRequestCommand
import org.appjam.smashing.domain.auth.command.SignUpRequestCommand
import org.appjam.smashing.domain.auth.dto.response.SignInResponse
import org.appjam.smashing.domain.auth.dto.response.SignUpResponse
import org.appjam.smashing.domain.auth.social.SocialAuthServiceManager
import org.appjam.smashing.domain.sport.entity.Sport
import org.appjam.smashing.domain.sport.repository.SportRepository
import org.appjam.smashing.domain.tier.entity.Tier
import org.appjam.smashing.domain.tier.repository.TierRepository
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.appjam.smashing.domain.user.repository.UserRepository
import org.appjam.smashing.domain.user.repository.UserSportProfileRepository
import org.appjam.smashing.global.auth.jwt.components.JwtProvider
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService(
    private val socialAuthServiceManager: SocialAuthServiceManager,
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
    private val sportRepository: SportRepository,
    private val tierRepository: TierRepository,
    private val userSportProfileRepository: UserSportProfileRepository,
) {
    fun signIn(requestCommand: SignInRequestCommand): SignInResponse {
        val kakaoId = socialAuthServiceManager.getKakaoId(requestCommand.accessToken)

        val user = userRepository.findByKakaoId(kakaoId)
            ?: return SignInResponse(
                accessToken = null,
                refreshToken = null,
                authId = kakaoId,
            )

        val userId = user.id ?: throw CustomException(ErrorCode.INTERNAL_SERVER_ERROR)

        val token = jwtProvider.issueToken(userId)

        return SignInResponse(
            accessToken = token.accessToken.token,
            refreshToken = token.refreshToken.token,
            authId = kakaoId,
        )
    }

    fun signUp(requestCommand: SignUpRequestCommand): SignUpResponse {
        validateUser(requestCommand)

        val sportCode = requestCommand.sportCode
        val sport = sportRepository.save(
            Sport(
                code = sportCode.name,
                name = sportCode.sportName,
            )
        )

        val tierType = requestCommand.tier
        if (tierType.initTier == null) {
            throw CustomException(ErrorCode.INVALID_INITIAL_TIER)
        }
        val tier = tierRepository.save(
            Tier(
                name = tierType.tierName,
                orderNo = tierType.orderNo,
                minLp = tierType.minLp,
                maxLp = tierType.maxLp,
                sport = sport,
            )
        )

        val user = userRepository.save(
            User(
                kakaoId = requestCommand.authId,
                nickname = requestCommand.nickname,
                gender = requestCommand.gender,
                openchatUrl = requestCommand.openChatUrl,
                region = requestCommand.region,
            )
        )

        val profile = userSportProfileRepository.save(
            UserSportProfile(
                lp = tierType.initTier,
                user = user,
                sport = sport,
                tier = tier,
            )
        )

        user.activeUserSportProfileId = profile.id

        val userId = user.id ?: throw CustomException(ErrorCode.NOT_FOUND)
        val token = jwtProvider.issueToken(userId)

        return SignUpResponse(
            accessToken = token.accessToken.token,
            refreshToken = token.refreshToken.token,
        )
    }

    private fun validateUser(requestCommand: SignUpRequestCommand) {
        if (userRepository.existsByKakaoId(requestCommand.authId)) {
            throw CustomException(ErrorCode.DUPLICATE_USER)
        }

        if (userRepository.existsByNickname(requestCommand.nickname)) {
            throw CustomException(ErrorCode.DUPLICATE_NICKNAME)
        }
    }
}
