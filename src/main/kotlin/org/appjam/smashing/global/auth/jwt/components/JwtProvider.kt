package org.appjam.smashing.global.auth.jwt.components

import org.appjam.smashing.global.auth.jwt.components.JwtGenerator.Companion.ROLES_KEY
import org.appjam.smashing.global.auth.jwt.components.JwtGenerator.Companion.TYPE_KEY
import org.appjam.smashing.global.auth.jwt.dto.TokenDto
import org.appjam.smashing.global.auth.jwt.enums.TokenType
import org.appjam.smashing.global.auth.security.data.CustomUserDetails
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
            accessTokenExpiredAt = access.expiredAt,
            refreshToken = refresh.token,
            refreshTokenExpiredAt = refresh.expiredAt,
        )
    }

    fun getAuthentication(
        token: String,
        timeZone: String
    ): Authentication {
        val claims = jwtValidator.parseAccessToken(token)

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
            authorities = authorities,
            timeZone = timeZone
        )

        return UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )
    }

    /**
     * 엑세스 토큰의 TTL(Time-To-Live)
     *
     * @param token 엑세스 토큰
     * @return 만료 시각에서 현재 시각을 뺀 유효 시간 반환 (만료된 경우 0 반환)
     */
    fun getAccessTtlMillis(
        token: String,
    ): Long {
        val claims = jwtValidator.parseAccessToken(token)
        val expirationMillis = claims.expiration.time

        return (expirationMillis - System.currentTimeMillis()).coerceAtLeast(0)
    }

    /**
     * 리프레시 토큰의 TTL(Time-To-Live)
     *
     * @param token 리프레시 토큰
     * @return 만료 시각에서 현재 시각을 뺀 유효 시간 반환 (만료된 경우 0 반환)
     */
    fun getRefreshTtlMillis(
        token: String,
    ): Long {
        val claims = jwtValidator.parseRefreshToken(token)
        val expirationMillis = claims.expiration.time

        return (expirationMillis - System.currentTimeMillis()).coerceAtLeast(0)
    }

    /**
     * 엑세스 토큰에서 subject 추출
     *
     * @param token 엑세스 토큰
     * @return  subject 추출하여 반환
     */
    fun extractSubject(
        token: String,
    ): String {
        val claims = jwtValidator.parseAccessToken(token)

        val type = claims[TYPE_KEY] as? String
        if (type != TokenType.ACCESS_TOKEN.name) {
            throw CustomException(ErrorCode.INVALID_ACCESS_TOKEN_TYPE)
        }

        val subject = claims.subject ?: throw CustomException(ErrorCode.INVALID_ACCESS_TOKEN_SUBJECT)

        return subject
    }

    companion object {
        private const val ROLE = "ROLE_"
    }
}
