package org.appjam.smashing.domain.auth.service

import org.appjam.smashing.domain.auth.command.reqeust.SignInRequestCommand
import org.appjam.smashing.domain.auth.command.reqeust.SignUpRequestCommand
import org.appjam.smashing.domain.auth.command.response.SignInResponseCommand
import org.appjam.smashing.domain.auth.command.response.SignUpResponseCommand
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
    fun signIn(requestCommand: SignInRequestCommand): SignInResponseCommand {
        val kakaoId = socialAuthServiceManager.getKakaoId(requestCommand.accessToken)

        val user = userRepository.findByKakaoId(
            kakaoId = kakaoId
        )

        if (user == null) {
            return SignInResponseCommand(
                token = null,
                authId = kakaoId,
            )
        }

        val userId = user.id ?: throw CustomException(ErrorCode.INTERNAL_SERVER_ERROR)

        val token = jwtProvider.issueToken(userId)

        return SignInResponseCommand(
            token = token,
            authId = kakaoId,
        )
    }

    fun signUp(
        authId: String,
        requestCommand: SignUpRequestCommand
    ): SignUpResponseCommand {
        if (userRepository.existsByKakaoId(authId)) {
            throw CustomException(ErrorCode.ALREADY_REGISTERED_USER)
        }

        val user = User(
            kakaoId = authId,
            nickname = requestCommand.nickname,
            gender = Gender.valueOf(requestCommand.gender),
            openchatUrl = requestCommand.openChatUrl,
        )

        userRepository.save(user)

        val userId = user.id ?: throw CustomException(ErrorCode.NOT_FOUND)

        val token = jwtProvider.issueToken(userId)

        return SignUpResponseCommand(
            token = token
        )
    }
}
