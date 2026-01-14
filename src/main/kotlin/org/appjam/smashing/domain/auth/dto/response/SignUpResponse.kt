package org.appjam.smashing.domain.auth.dto.response

data class SignUpResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String?,
)
