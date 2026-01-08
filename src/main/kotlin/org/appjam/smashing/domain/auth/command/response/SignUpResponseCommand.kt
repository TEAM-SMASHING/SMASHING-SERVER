package org.appjam.smashing.domain.auth.command.response

import org.appjam.smashing.domain.auth.dto.response.SignUpResponse

data class SignUpResponseCommand(
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        fun SignUpResponseCommand.toDto(): SignUpResponse = SignUpResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}
