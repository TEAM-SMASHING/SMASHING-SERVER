package org.appjam.smashing.domain.auth.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "jwt")
class JwtProperties {
    lateinit var secret: String
    var accessTokenExpireTime: Long = 0
    var refreshTokenExpireTime: Long = 0
}
