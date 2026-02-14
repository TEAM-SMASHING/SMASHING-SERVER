package org.appjam.smashing.global.auth.jwt.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.appjam.smashing.global.auth.jwt.components.JwtProvider
import org.appjam.smashing.global.auth.jwt.handler.JwtAuthenticationEntryPoint.Companion.EXCEPTION_KEY
import org.appjam.smashing.global.config.TimeZoneProperties
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.ZoneId

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val jwtBlacklistManager: JwtBlacklistManager,
    private val timeZoneProperties: TimeZoneProperties,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveToken(request)

        if (!token.isNullOrBlank()) {
            try {
                if (jwtBlacklistManager.contains(token)) {
                    SecurityContextHolder.clearContext()
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    return
                }

                val timeZone = resolveTimeZone(request)
                val authentication = jwtProvider.getAuthentication(token, timeZone)
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: Exception) {
                SecurityContextHolder.clearContext()
                request.setAttribute(EXCEPTION_KEY, e)
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        val prefix = PREFIX

        return if (header.startsWith(prefix)) {
            header.substring(prefix.length).trim().takeIf { it.isNotBlank() }
        } else {
            null
        }
    }

    // TODO: DB UTC 변경시 추후 사용자 타임존 관련 처리 필요
    private fun resolveTimeZone(
        request: HttpServletRequest
    ): String {
        val timeZone = request.getHeader(TIME_ZONE_HEADER)?.trim()

        if (timeZone.isNullOrBlank()) {
            return timeZoneProperties.defaultTimeZone
        }

        return runCatching {
            ZoneId.of(timeZone)
            timeZone
        }.getOrElse {
            timeZoneProperties.defaultTimeZone
        }
    }

    companion object {
        private const val PREFIX = "Bearer "
        private const val TIME_ZONE_HEADER = "Time-Zone"
    }
}
