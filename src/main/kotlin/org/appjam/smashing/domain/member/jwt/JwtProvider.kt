package org.appjam.smashing.domain.member.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
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

    fun getUserId(token: String): Long {
        val jws: Jws<Claims> = jwtValidator.parseToken(token)
        val subject = jws.body.subject

        return subject.toLongOrNull()
            ?: throw CustomException(ErrorCode.INVALID_TOKEN_SUBJECT)
    }
}
