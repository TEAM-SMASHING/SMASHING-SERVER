package org.appjam.smashing.domain.auth.exception

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.appjam.smashing.global.common.dto.ApiResponse
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val errorCode = ErrorCode.UNAUTHORIZED

        response.status = errorCode.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "utf-8"
        response.writer.write(objectMapper.writeValueAsString(ApiResponse.errorBody(errorCode)))
    }

}
