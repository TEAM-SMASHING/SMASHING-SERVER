package org.appjam.smashing.domain.auth.jwt

import org.appjam.smashing.domain.auth.jwt.JwtGenerator.Companion.TYPE_KEY
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component
class JwtProvider(
    private val jwtGenerator: JwtGenerator,
    private val jwtValidator: JwtValidator,
) {
    fun issueToken(userId: String): Token = Token.of(
        jwtGenerator.generateAccessToken(userId),
        jwtGenerator.generateRefreshToken()
    )

    fun getAuthentication(token: String): Authentication {
        val claims = jwtValidator.validateAndParseAccessToken(token)

        val type = claims[TYPE_KEY] as? String
        if (type != TokenType.ACCESS_TOKEN.name) {
            throw CustomException(ErrorCode.INVALID_ACCESS_TOKEN_TYPE)
        }

        val subject = claims.subject ?: throw CustomException(ErrorCode.INVALID_ACCESS_TOKEN_SUBJECT)
        val role = listOf(SimpleGrantedAuthority("ROLE_USER"))

        val userDetails = CustomUserDetails(
            userId = subject,
            authorities = role
        )

        return UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )
    }
}
