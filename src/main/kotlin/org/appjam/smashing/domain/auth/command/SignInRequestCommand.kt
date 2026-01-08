package org.appjam.smashing.domain.auth.command

import org.appjam.smashing.domain.auth.dto.request.SignInRequest

data class SignInRequestCommand(
    val accessToken: String,
) {
    companion object {
        fun SignInRequest.toCommand(): SignInRequestCommand =
            SignInRequestCommand(
                accessToken = accessToken,
            )
    }
}
