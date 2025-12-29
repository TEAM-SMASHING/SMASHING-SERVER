package org.appjam.smashing.domain.member.jwt

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
    fun generateToken(
        userId: Long,
        tokenType: TokenType,
    ): String {
        val now = Date()
        val expiration = generateExpirationDate(now, tokenType)

        return Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(keyProvider.getSigningKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    private fun generateExpirationDate(
        now: Date,
        tokenType: TokenType,
    ): Date = when (tokenType) {
        TokenType.ACCESS_TOKEN -> Date(now.time + jwtProperties.accessTokenExpireTime)
        TokenType.REFRESH_TOKEN -> Date(now.time + jwtProperties.refreshTokenExpireTime)
    }
}
