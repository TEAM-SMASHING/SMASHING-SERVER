package org.appjam.smashing.domain.auth.jwt

import io.jsonwebtoken.Header
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtGenerator(
    private val jwtProperties: JwtProperties,
    private val keyProvider: KeyProvider,
) {
    fun generateAccessToken(userId: String): String {
        val now = Date()
        val expiration = Date(now.time + jwtProperties.accessTokenExpireTime)

        return Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setSubject(userId)
            .claim(TYPE_KEY, TokenType.ACCESS_TOKEN.name)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(keyProvider.getSigningKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun generateRefreshToken(): String {
        val now = Date()
        val expiration = Date(now.time + jwtProperties.refreshTokenExpireTime)

        return Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setSubject(UUID.randomUUID().toString())
            .claim(TYPE_KEY, TokenType.REFRESH_TOKEN.name)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(keyProvider.getSigningKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    companion object {
        const val TYPE_KEY = "type"
    }
}
