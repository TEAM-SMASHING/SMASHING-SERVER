package org.appjam.smashing.domain.auth.service

import org.appjam.smashing.domain.auth.command.SignInRequestCommand
import org.appjam.smashing.domain.auth.command.SignUpRequestCommand
import org.appjam.smashing.domain.auth.dto.response.SignInResponse
import org.appjam.smashing.domain.auth.dto.response.SignUpResponse
import org.appjam.smashing.domain.auth.social.SocialAuthServiceManager
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.enums.Gender
import org.appjam.smashing.domain.user.repository.UserRepository
import org.appjam.smashing.global.auth.jwt.components.JwtProvider
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService(
    private val socialAuthServiceManager: SocialAuthServiceManager,
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
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

    fun signUp(
        authId: String,
        requestCommand: SignUpRequestCommand
    ): SignUpResponse {
        validateUser(
            authId = authId,
            requestCommand = requestCommand,
        )

        val user = User(
            kakaoId = authId,
            nickname = requestCommand.nickname,
            gender = Gender.valueOf(requestCommand.gender),
            openchatUrl = requestCommand.openChatUrl,
            region = requestCommand.region,
        )
        // 유저가 생성되면 유저프로필도 생성해야 됨
        // sportCode, tier 추가하기

        userRepository.save(user)

        val userId = user.id ?: throw CustomException(ErrorCode.NOT_FOUND)

        val token = jwtProvider.issueToken(userId)

        return SignUpResponse(
            accessToken = token.accessToken.token,
            refreshToken = token.refreshToken.token,
        )
    }

    private fun validateUser(
        authId: String,
        requestCommand: SignUpRequestCommand
    ) {
        if (userRepository.existsByKakaoId(authId)) {
            throw CustomException(ErrorCode.DUPLICATE_USER)
        }

        if (userRepository.existsByNickname(requestCommand.nickname)) {
            throw CustomException(ErrorCode.DUPLICATE_NICKNAME)
        }
    }
}
