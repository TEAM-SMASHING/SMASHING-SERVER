package org.appjam.smashing.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val errorCode: String,
    val message: String,
) {

    /* ========== 공통 ========== */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ISE", "서버 내부 오류입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FBD", "권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNA", "인증되지 않았습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "MNA", "지원하지 않는 HTTP 메서드입니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "IVP", "요청 파라미터 타입/형식이 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NF", "존재하지 않는 리소스입니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BR", "잘못된 요청입니다."),

    /* ========== 도메인 ========== */
    // Auth - Access Token
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-001", "만료된 엑세스 토큰입니다."),
    INVALID_ACCESS_SIGNATURE(HttpStatus.UNAUTHORIZED, "AUTH-002", "변조된 엑세스 토큰입니다."),
    MALFORMED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-003", "엑세스 토큰의 형식이 잘못되었습니다."),
    UNSUPPORTED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-004", "지원되지 않는 엑세스 토큰 형식입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-005", "유효하지 않은 엑세스 토큰입니다."),
    INVALID_ACCESS_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "AUTH-006", "엑세스 토큰의 타입이 올바르지 않습니다."),
    INVALID_ACCESS_TOKEN_SUBJECT(HttpStatus.UNAUTHORIZED, "AUTH-007", "엑세스 토큰의 유저 정보가 올바르지 않습니다."),
    INVALID_ACCESS_TOKEN_CONTENTS(HttpStatus.UNAUTHORIZED, "AUTH-008", "유효하지 않은 정보가 담긴 엑세스 토큰입니다."),
    INVALID_ACCESS_TOKEN_CLAIM(HttpStatus.UNAUTHORIZED, "AUTH-009", "유효하지 않은 권한이 담긴 엑세스 토큰입니다."),

    // Auth - Refresh Token
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-010", "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-011", "만료된 리프레시 토큰입니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH-012", "저장된 리프레시 토큰과 일치하지 않습니다."),
    INVALID_REFRESH_SIGNATURE(HttpStatus.UNAUTHORIZED, "AUTH-013", "변조된 리프레시 토큰입니다."),
    MALFORMED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-014", "리프레시 토큰의 형식이 잘못되었습니다."),
    UNSUPPORTED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-015", "지원되지 않는 리프레시 토큰 형식입니다."),
    INVALID_REFRESH_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "AUTH-016", "리프레시 토큰의 타입이 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN_CONTENTS(HttpStatus.UNAUTHORIZED, "AUTH-017", "유효하지 않은 정보가 담긴 리프레시 토큰입니다."),

    // Domain - Matching
    MATCHING_REQUESTER_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH-001", "요청자 유저를 찾을 수 없습니다."),
    MATCHING_RECEIVER_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH-002", "상대 유저 스포츠 프로필을 찾을 수 없습니다."),
    MATCHING_RECEIVER_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH-003", "상대 유저 정보를 찾을 수 없습니다."),
    MATCHING_CANNOT_REQUEST_TO_SELF(HttpStatus.BAD_REQUEST, "MATCH-004", "본인에게 매칭을 신청할 수 없습니다."),
    MATCHING_DAILY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "MATCH-005", "해당 유저와 오늘 매칭 가능 횟수를 초과했습니다."),

    // Domain - Notification
    NOTIFICATION_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTI-001", "알림 템플릿을 찾을 수 없습니다."),

}
