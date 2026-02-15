package org.appjam.smashing.global.auth.jwt.filter

import org.appjam.smashing.global.auth.jwt.components.JwtProvider
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class JwtBlacklistManager(
    private val redis: StringRedisTemplate,
    private val jwtProvider: JwtProvider,
) {
    fun add(authorizationOrToken: String) {
        val token = normalize(authorizationOrToken)
        if (token.isBlank()) return

        val ttlMillis = jwtProvider.getAccessTtlMills(token)
        if (ttlMillis <= 0) return

        val ttlSeconds = (ttlMillis / MILLIS_PER_SECOND).coerceAtLeast(1)

        redis.opsForValue().set(key(token), "1", ttlSeconds, TimeUnit.SECONDS)
    }

    fun contains(authorizationOrToken: String): Boolean {
        val token = normalize(authorizationOrToken)
        if (token.isBlank()) return false

        return redis.hasKey(key(token))
    }

    private fun key(token: String) = BLACKLIST_PREFIX + token

    private fun normalize(token: String?): String {
        if (token.isNullOrBlank()) return ""
        val trimmed = token.trim()

        return if (trimmed.startsWith(BEARER_PREFIX)) trimmed.substring(BEARER_PREFIX.length).trim() else trimmed
    }

    companion object {
        private const val BLACKLIST_PREFIX = "blackList:"
        private const val BEARER_PREFIX = "Bearer "
        private const val MILLIS_PER_SECOND = 1000
    }
}
