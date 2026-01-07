package org.appjam.smashing.domain.auth.dto.response

data class SignInResponseDto(
    val accessToken: String?,
    val refreshToken: String?,
    val authId: String?,
)
