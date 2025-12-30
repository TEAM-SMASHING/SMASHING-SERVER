package org.appjam.smashing.domain.auth.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.appjam.smashing.domain.auth.jwt.JwtProvider
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
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
        val token = resolveAccessToken(request)

        if (token == null) {
            filterChain.doFilter(request, response)
            return
        }

        val userId = jwtProvider.getUserId(token)

        setAuthentication(userId)

        filterChain.doFilter(request, response)
    }

    private fun resolveAccessToken(request: HttpServletRequest): String? {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        val prefix = PREFIX
        if (!header.startsWith(prefix)) return null

        return header.substring(prefix.length).trim().takeIf { it.isNotBlank() }
    }

    private fun setAuthentication(userId: Long) {
        val authentication = UsernamePasswordAuthenticationToken(
            userId,
            null,
            emptyList()
        )
        SecurityContextHolder.getContext().authentication = authentication
    }

    companion object {
        private const val PREFIX = "Bearer "
    }
}
