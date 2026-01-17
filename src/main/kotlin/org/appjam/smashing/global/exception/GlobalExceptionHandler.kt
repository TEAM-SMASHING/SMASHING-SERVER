package org.appjam.smashing.global.exception

import org.appjam.smashing.global.common.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * 커스텀 예외
     */
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(exception: CustomException): ResponseEntity<ApiResponse<Unit>> {
        return ApiResponse.error(exception.errorCode)
    }

    /**
     * 존재하지 않는 API 매핑 (404)
     */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(exception: NoHandlerFoundException): ResponseEntity<ApiResponse<Unit>> {
        return ApiResponse.error(ErrorCode.NOT_FOUND)
    }

    /**
     * 존재하지 않는 정적 리소스 (404)
     */
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(exception: NoResourceFoundException): ResponseEntity<ApiResponse<Unit>> {
        return ApiResponse.error(ErrorCode.NOT_FOUND)
    }

    /**
     * HTTP 메서드 불일치 (405)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(exception: HttpRequestMethodNotSupportedException): ResponseEntity<ApiResponse<Unit>> {
        return ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED)
    }

    /**
     * 요청 파라미터 타입 변환 실패
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(exception: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Unit>> {
        return ApiResponse.error(ErrorCode.INVALID_PARAMETER)
    }

    /**
     * Validation 실패 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(exception: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> {
        val message = exception.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
            ?: exception.bindingResult.allErrors.firstOrNull()?.defaultMessage
            ?: ErrorCode.BAD_REQUEST.message

        return ApiResponse.fail(message)
    }

    /**
     * 권한 거부
     */
    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDeniedException(exception: AuthorizationDeniedException): ResponseEntity<ApiResponse<Unit>> {
        return ApiResponse.error(ErrorCode.FORBIDDEN)
    }

    /**
     * 그 외
     */
    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<ApiResponse<Unit>> {
        return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR)
    }
}
