package org.appjam.smashing.domain.auth.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class JwtProvider(
    private val jwtGenerator: JwtGenerator,
    private val jwtValidator: JwtValidator,
) {
    fun issueToken(userId: Long): Token = Token.of(
        jwtGenerator.generateToken(userId, TokenType.ACCESS_TOKEN),
        jwtGenerator.generateToken(userId, TokenType.REFRESH_TOKEN)
    )

    fun getAuthentication(token: String): Authentication {
        val jws: Jws<Claims> = jwtValidator.parseToken(token)
        val subject = jws.body.subject

        val userId = subject.toLongOrNull()
            ?: throw CustomException(ErrorCode.INVALID_TOKEN_SUBJECT)

        val userDetails = CustomUserDetails(userId)

        return UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )
    }
}
