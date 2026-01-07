package org.appjam.smashing.domain.auth.dto.response

data class SignUpResponseDto(
    val accessToken: String,
    val refreshToken: String,
)
