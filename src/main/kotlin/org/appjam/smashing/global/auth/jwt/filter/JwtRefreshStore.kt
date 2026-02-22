package org.appjam.smashing.global.auth.jwt.filter

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class JwtRefreshStore(
    private val redis: StringRedisTemplate
) {
    private fun userKey(userId: String) = USER_PREFIX + userId

    private fun tokenKey(refreshToken: String) = TOKEN_PREFIX + refreshToken

    /**
     * Redis에 리프레시 토큰 저장
     */
    fun save(
        userId: String,
        refreshToken: String,
        ttlMillis: Long,
    ) {
        if (ttlMillis <= 0) return

        val ttlSeconds = (ttlMillis / MILLIS_PER_SECOND).coerceAtLeast(1)

        redis.opsForSet().add(userKey(userId), refreshToken)

        redis.opsForValue().set(tokenKey(refreshToken), userId, ttlSeconds, TimeUnit.SECONDS)
    }

    /**
     * 유저에게 저장된 모든 리프레시 토큰을 삭제
     */
    fun deleteAllForUser(userId: String) {
        val userRefreshTokens = userKey(userId)

        val tokens: Set<String>? = redis.opsForSet().members(userRefreshTokens)

        if (!tokens.isNullOrEmpty()) {
            redis.delete(tokens.map { tokenKey(it) })
        }

        redis.delete(userRefreshTokens)
    }

    companion object {
        private const val USER_PREFIX = "rt:user:"
        private const val TOKEN_PREFIX = "rt:token:"
        private const val MILLIS_PER_SECOND = 1000
    }
}
