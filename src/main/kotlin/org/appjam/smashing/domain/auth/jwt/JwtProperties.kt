package org.appjam.smashing.domain.auth.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpireTime: Long,
    val refreshTokenExpireTime: Long,
)
