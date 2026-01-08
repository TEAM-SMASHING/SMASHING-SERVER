package org.appjam.smashing.domain.auth.command

import org.appjam.smashing.domain.auth.dto.SignInRequestDto

data class SignInRequestCommand(
    val accessToken: String,
) {
    companion object {
        fun SignInRequestDto.toCommand(): SignInRequestCommand =
            SignInRequestCommand(
                accessToken = accessToken,
            )
    }
}
