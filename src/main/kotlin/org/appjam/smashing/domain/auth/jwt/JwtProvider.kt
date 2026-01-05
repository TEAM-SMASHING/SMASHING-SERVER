package org.appjam.smashing.domain.auth.jwt

import org.appjam.smashing.domain.auth.jwt.JwtGenerator.Companion.ROLES_KEY
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
    fun issueToken(
        userId: String,
        roles: List<String> = emptyList()
    ): TokenDto {
        val access = jwtGenerator.generateAccessToken(userId = userId, roles = roles)
        val refresh = jwtGenerator.generateRefreshToken()

        return TokenDto.of(
            accessToken = access.token,
            refreshToken = refresh.token,
            accessExpireTime = access.expireTime,
            refreshExpireTime = refresh.expireTime,
        )
    }

    fun getAuthentication(token: String): Authentication {
        val claims = jwtValidator.validateAndParseAccessToken(token)

        val roles = claims[ROLES_KEY] as? List<*> ?: throw CustomException(ErrorCode.INVALID_ACCESS_TOKEN_CLAIM)
        val authorities = roles.map {
            SimpleGrantedAuthority(ROLE + it.toString())
        }

        val type = claims[TYPE_KEY] as? String
        if (type != TokenType.ACCESS_TOKEN.name) {
            throw CustomException(ErrorCode.INVALID_ACCESS_TOKEN_TYPE)
        }

        val subject = claims.subject ?: throw CustomException(ErrorCode.INVALID_ACCESS_TOKEN_SUBJECT)

        val userDetails = CustomUserDetails(
            userId = subject,
            authorities = authorities
        )

        return UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )
    }

    companion object {
        private const val ROLE = "ROLE_"
    }
}
