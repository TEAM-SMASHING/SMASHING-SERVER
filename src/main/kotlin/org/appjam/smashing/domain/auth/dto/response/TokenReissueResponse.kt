package org.appjam.smashing.domain.auth.dto.response

data class TokenReissueResponse(
    val accessToken: String,
    val refreshToken: String,
)
