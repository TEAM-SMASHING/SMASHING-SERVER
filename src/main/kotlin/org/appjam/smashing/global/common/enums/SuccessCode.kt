package org.appjam.smashing.global.common.enums

import org.springframework.http.HttpStatus

enum class SuccessCode(
    val httpStatus: HttpStatus,
    val successCode: String,
    val message: String,
) {
    // Auth
    ACCEPTED(HttpStatus.ACCEPTED, "AUTH-001", "회원가입이 필요합니다.")
}
