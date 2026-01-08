package org.appjam.smashing.domain.auth.command

import org.appjam.smashing.domain.auth.dto.response.SignInResponse
import org.appjam.smashing.global.auth.jwt.dto.TokenDto

data class SignInResponseCommand(
    val token: TokenDto?,
    val authId: String?
) {
    companion object {
        fun SignInResponseCommand.toDto(): SignInResponse =
            SignInResponse(
                accessToken = token?.accessToken?.token,
                refreshToken = token?.refreshToken?.token,
                authId = authId,
            )
    }
}
