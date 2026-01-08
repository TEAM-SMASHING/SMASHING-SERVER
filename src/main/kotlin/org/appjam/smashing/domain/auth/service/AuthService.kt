package org.appjam.smashing.domain.auth.service

import org.appjam.smashing.domain.auth.command.reqeust.SignInRequestCommand
import org.appjam.smashing.domain.auth.command.reqeust.SignUpRequestCommand
import org.appjam.smashing.domain.auth.command.response.SignInResponseCommand
import org.appjam.smashing.domain.auth.social.SocialAuthServiceManager
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
    fun signIn(requestCommand: SignInRequestCommand): SignInResponseCommand {
        val kakaoId = socialAuthServiceManager.getKakaoId(requestCommand.accessToken)

        val user = userRepository.findByKakaoId(
            kakaoId = kakaoId
        )

        if (user == null) {
            return SignInResponseCommand(
                accessToken = null,
                refreshToken = null,
                authId = kakaoId,
            )
        }

        val userId = user.id ?: throw CustomException(ErrorCode.INTERNAL_SERVER_ERROR)

        val token = jwtProvider.issueToken(userId)

        return SignInResponseCommand(
            accessToken = token.accessToken.token,
            refreshToken = token.refreshToken.token,
            authId = null,
        )
    }

    fun signUp(requestCommand: SignUpRequestCommand) {

    }
}
