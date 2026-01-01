package org.appjam.smashing.domain.auth.exception

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.appjam.smashing.global.common.dto.ApiResponse
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class ExceptionHandlerFilter(
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (e: CustomException) {
            handleException(response, e.errorCode)
        } catch (e: Exception) {
            handleException(response, ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    private fun handleException(
        response: HttpServletResponse,
        errorCode: ErrorCode,
    ) {
        response.status = errorCode.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "utf-8"
        response.writer.write(objectMapper.writeValueAsString(ApiResponse.errorBody(errorCode)))
    }

}
