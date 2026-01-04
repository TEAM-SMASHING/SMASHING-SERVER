package org.appjam.smashing.domain.auth.exception

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.appjam.smashing.global.exception.CustomException
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class ExceptionHandlerFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (e: CustomException) {
            request.setAttribute(EXCEPTION_KEY, e)
            throw e
        } catch (e: Exception) {
            request.setAttribute(EXCEPTION_KEY, e)
            throw e
        }
    }

    companion object {
        const val EXCEPTION_KEY = "exception"
    }
}
