package org.appjam.smashing.global.common.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.appjam.smashing.global.common.enums.Status
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.OffsetDateTime

data class ApiResponse<T>(
    val status: Status,
    val statusCode: Int,
    val data: T? = null,
    @field:JsonInclude(JsonInclude.Include.NON_NULL) val message: String? = null,
    @field:JsonInclude(JsonInclude.Include.NON_NULL) val errorCode: String? = null,
    @field:JsonInclude(JsonInclude.Include.NON_NULL) val errorName: String? = null,
    val timestamp: OffsetDateTime = OffsetDateTime.now(),
) {
    companion object {

        /**
         * 성공 응답
         *
         * @param data 응답 데이터
         * @return ResponseEntity
         */
        fun <T> success(data: T): ResponseEntity<ApiResponse<T>> = ResponseEntity.ok(
                ApiResponse(
                    status = Status.SUCCESS,
                    statusCode = 200,
                    data = data,
                )
            )

        /**
         * 성공 응답
         *
         * @return ResponseEntity
         */
        fun success(): ResponseEntity<ApiResponse<Unit>> = ResponseEntity.ok(
                ApiResponse(
                    status = Status.SUCCESS,
                    statusCode = 200,
                )
            )

        /**
         * 실패 응답
         *
         * @param message 메시지
         * @return ResponseEntity
         */
        fun fail(message: String?): ResponseEntity<ApiResponse<Unit>> = ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                    ApiResponse(
                        status = Status.FAIL,
                        statusCode = 400,
                        message = message,
                    )
                )

        /**
         * 에러 응답
         *
         * @param errorCode 에러 코드
         * @return ResponseEntity
         */
        fun error(errorCode: ErrorCode): ResponseEntity<ApiResponse<Unit>> = ResponseEntity
                .status(errorCode.httpStatus)
                .body(
                    ApiResponse(
                        status = Status.ERROR,
                        statusCode = errorCode.httpStatus.value(),
                        message = errorCode.message,
                        errorCode = errorCode.errorCode,
                        errorName = errorCode.name,
                    )
                )
    }
}
