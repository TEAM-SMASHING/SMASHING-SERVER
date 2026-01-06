package org.appjam.smashing.domain.auth.command

import org.appjam.smashing.domain.auth.dto.SignInResponseDto

data class SignInResponseCommand(
    val accessToken: String?,
    val refreshToken: String?,
    val kakaoId: String?
) {
    companion object {
        fun SignInResponseCommand.toDto(): SignInResponseDto =
            SignInResponseDto(
                accessToken = accessToken,
                refreshToken = refreshToken,
                kakaoId = kakaoId,
            )
    }
}
