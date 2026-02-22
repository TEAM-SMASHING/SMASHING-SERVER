package org.appjam.smashing.global.auth.jwt.components

import io.jsonwebtoken.*
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.stereotype.Component

@Component
class JwtValidator(
    private val keyProvider: KeyProvider
) {
    fun parseRefreshToken(token: String): Claims =
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

    fun parseAccessToken(token: String): Claims =
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
