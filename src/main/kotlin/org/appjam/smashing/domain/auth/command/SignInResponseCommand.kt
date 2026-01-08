package org.appjam.smashing.domain.auth.command

import org.appjam.smashing.domain.auth.dto.response.SignInResponse

data class SignInResponseCommand(
    val accessToken: String?,
    val refreshToken: String?,
    val authId: String
) {
    companion object {
        fun SignInResponseCommand.toDto(): SignInResponse =
            SignInResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                authId = authId,
            )
    }
}
