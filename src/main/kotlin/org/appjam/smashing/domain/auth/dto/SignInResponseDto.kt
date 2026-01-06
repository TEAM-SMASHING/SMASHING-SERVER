package org.appjam.smashing.domain.auth.dto

data class SignInResponseDto(
    val accessToken: String?,
    val refreshToken: String?,
    val kakaoId: String?,
)
