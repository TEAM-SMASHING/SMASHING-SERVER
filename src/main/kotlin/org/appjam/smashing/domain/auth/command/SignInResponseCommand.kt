package org.appjam.smashing.domain.auth.command

import org.appjam.smashing.domain.auth.dto.SignInResponseDto

data class SignInResponseCommand(
    val accessToken: String?,
    val refreshToken: String?,
    val authId: String?
) {
    companion object {
        fun SignInResponseCommand.toDto(): SignInResponseDto =
            SignInResponseDto(
                accessToken = accessToken,
                refreshToken = refreshToken,
                authId = authId,
            )
    }
}
