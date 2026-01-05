package org.appjam.smashing.domain.auth.jwt

import io.jsonwebtoken.Header
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Component
import java.util.*

data class GeneratedToken(
    val token: String,
    val expireTime: Long,
)

@Component
class JwtGenerator(
    private val jwtProperties: JwtProperties,
    private val keyProvider: KeyProvider,
) {
    fun generateAccessToken(
        userId: String,
        roles: List<String>,
    ): GeneratedToken {
        val now = Date()
        val expireTime = now.time + jwtProperties.accessTokenExpireTime
        val expiration = Date(expireTime)

        val accessToken = Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setSubject(userId)
            .claim(TYPE_KEY, TokenType.ACCESS_TOKEN.name)
            .claim(ROLES_KEY, roles)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(keyProvider.getSigningKey(), SignatureAlgorithm.HS256)
            .compact()

        return GeneratedToken(
            token = accessToken,
            expireTime = expireTime,
        )
    }

    fun generateRefreshToken(): GeneratedToken {
        val now = Date()
        val expireTime = now.time + jwtProperties.refreshTokenExpireTime
        val expiration = Date(expireTime)

        val refreshToken = Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setSubject(UUID.randomUUID().toString())
            .claim(TYPE_KEY, TokenType.REFRESH_TOKEN.name)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(keyProvider.getSigningKey(), SignatureAlgorithm.HS256)
            .compact()

        return GeneratedToken(
            token = refreshToken,
            expireTime = expireTime,
        )
    }

    companion object {
        const val TYPE_KEY = "type"
        const val ROLES_KEY = "roles"
    }
}
