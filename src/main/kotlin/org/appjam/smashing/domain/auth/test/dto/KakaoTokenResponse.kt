package org.appjam.smashing.domain.auth.test.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class KakaoTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)
