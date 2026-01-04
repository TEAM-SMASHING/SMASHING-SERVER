package org.appjam.smashing.domain.auth.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.appjam.smashing.domain.auth.exception.JwtAuthenticationEntryPoint.Companion.EXCEPTION_KEY
import org.appjam.smashing.domain.auth.jwt.JwtProvider
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val token = resolveToken(request)

            if (token == null) {
                filterChain.doFilter(request, response)
                return
            }

            val authentication = jwtProvider.getAuthentication(token)
            SecurityContextHolder.getContext().authentication = authentication

            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            request.setAttribute(EXCEPTION_KEY, e)
            throw e
        }
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

    companion object {
        private const val PREFIX = "Bearer "
    }
}
