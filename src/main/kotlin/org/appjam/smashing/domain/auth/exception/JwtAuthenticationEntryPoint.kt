package org.appjam.smashing.domain.auth.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.appjam.smashing.domain.auth.exception.ExceptionHandlerFilter.Companion.EXCEPTION_KEY
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver

@Component
class JwtAuthenticationEntryPoint(
    @Qualifier("handlerExceptionResolver")
    private val resolver: HandlerExceptionResolver,
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val exception = request.getAttribute(EXCEPTION_KEY) as? Exception ?: authException

        resolver.resolveException(request, response, null, exception)
    }

}
