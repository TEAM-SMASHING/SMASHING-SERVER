package org.appjam.smashing.domain.auth.jwt

import io.jsonwebtoken.*
import org.appjam.smashing.domain.auth.jwt.JwtGenerator.Companion.TYPE_KEY
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.stereotype.Component
import java.security.MessageDigest

@Component
class JwtValidator(
    private val keyProvider: KeyProvider
) {
    fun validateRefreshToken(
        refreshToken: String,
        storedRefreshToken: String
    ) {
        val claims = parseRefreshToken(refreshToken)

        val type = claims[TYPE_KEY] as? String
        if (type != TokenType.REFRESH_TOKEN.name) {
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN_TYPE)
        }

        val isMatched = MessageDigest.isEqual(
            refreshToken.toByteArray(),
            storedRefreshToken.toByteArray()
        )

        if (!isMatched) {
            throw CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH)
        }
    }

    private fun parseRefreshToken(token: String): Claims =
        try {
            getJwtParser().parseClaimsJws(token).body
        } catch (e: ExpiredJwtException) {
            throw CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN)
        } catch (e: UnsupportedJwtException) {
            throw CustomException(ErrorCode.UNSUPPORTED_REFRESH_TOKEN)
        } catch (e: MalformedJwtException) {
            throw CustomException(ErrorCode.MALFORMED_REFRESH_TOKEN)
        } catch (e: SecurityException) {
            throw CustomException(ErrorCode.INVALID_REFRESH_SIGNATURE)
        } catch (e: IllegalArgumentException) {
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN_CONTENTS)
        } catch (e: Exception) {
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

    fun validateAndParseAccessToken(token: String): Claims =
        try {
            getJwtParser().parseClaimsJws(token).body
        } catch (e: ExpiredJwtException) {
            throw CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN)
        } catch (e: UnsupportedJwtException) {
            throw CustomException(ErrorCode.UNSUPPORTED_ACCESS_TOKEN)
        } catch (e: MalformedJwtException) {
            throw CustomException(ErrorCode.MALFORMED_ACCESS_TOKEN)
        } catch (e: SecurityException) {
            throw CustomException(ErrorCode.INVALID_ACCESS_SIGNATURE)
        } catch (e: IllegalArgumentException) {
            throw CustomException(ErrorCode.INVALID_ACCESS_TOKEN_CONTENTS)
        } catch (e: Exception) {
            throw CustomException(ErrorCode.INVALID_ACCESS_TOKEN)
        }

    private fun getJwtParser(): JwtParser =
        Jwts.parserBuilder()
            .setSigningKey(keyProvider.getSigningKey())
            .build()
}
