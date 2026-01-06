package org.appjam.smashing.global.auth.jwt.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpireTime: Long,
    val refreshTokenExpireTime: Long,
)
