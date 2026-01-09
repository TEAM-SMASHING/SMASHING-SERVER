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

    // Auth - Kakao Token
    INVALID_KAKAO_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-018", "유효하지 않은 카카오 액세스 토큰입니다."),
    DUPLICATE_USER(HttpStatus.CONFLICT, "AUTH-019", "이미 존재하는 유저입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "AUTH-020", "이미 사용 중인 닉네임입니다."),

    // Domain - User / Profile
    USER_SPORT_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-001", "유저 스포츠 프로필을 찾을 수 없습니다."),

    // Domain - Matching
    MATCHING_REQUESTER_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH-001", "요청자 유저를 찾을 수 없습니다."),
    MATCHING_RECEIVER_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH-002", "상대 유저 스포츠 프로필을 찾을 수 없습니다."),
    MATCHING_RECEIVER_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH-003", "상대 유저 정보를 찾을 수 없습니다."),
    MATCHING_CANNOT_REQUEST_TO_SELF(HttpStatus.BAD_REQUEST, "MATCH-004", "본인에게 매칭을 신청할 수 없습니다."),
    MATCHING_DAILY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "MATCH-005", "해당 유저와 오늘 매칭 가능 횟수를 초과했습니다."),
    MATCHING_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH-006", "매칭을 찾을 수 없습니다."),
    MATCHING_FORBIDDEN(HttpStatus.FORBIDDEN, "MATCH-007", "해당 매칭에 대한 권한이 없습니다."),
    MATCHING_ALREADY_RESPONDED(HttpStatus.BAD_REQUEST, "MATCH-008", "이미 응답된 매칭 요청입니다."),

    // Domain - Game
    GAME_NOT_FOUND(HttpStatus.NOT_FOUND, "GAME-001", "경기를 찾을 수 없습니다."),
    GAME_FORBIDDEN(HttpStatus.FORBIDDEN, "GAME-002", "경기에 대한 권한이 없습니다."),
    GAME_RESULT_INVALID_PLAYERS(HttpStatus.BAD_REQUEST, "GAME-003", "승자 또는 패자가 경기 참여자가 아닙니다."),
    GAME_REVIEW_ONLY_FIRST_SUBMISSION_ALLOWED(HttpStatus.BAD_REQUEST, "GAME-004", "리뷰는 최초 제출에서만 가능합니다."),
    GAME_RESULT_SUBMIT_BLOCKED_1H(HttpStatus.BAD_REQUEST, "GAME-005", "경기 생성 후 1시간 동안 결과 제출이 불가합니다."),
    GAME_RESULT_SUBMIT_BLOCKED_10M(HttpStatus.BAD_REQUEST, "GAME-006", "연속 경기의 경우 생성 후 10분 동안 결과 제출이 불가합니다."),
    GAME_RESULT_INVALID_SCORE(HttpStatus.BAD_REQUEST, "GAME-007", "승자 점수는 패자 점수보다 커야 합니다."),
    GAME_RESULT_SAME_PLAYER(HttpStatus.BAD_REQUEST, "GAME-008", "winnerUserId와 loserUserId는 같을 수 없습니다."),
    GAME_RESULT_ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "GAME-009", "이미 결과 제출이 진행 중이거나 제출되었습니다."),
    GAME_REVIEW_REQUIRED_ON_FIRST_SUBMISSION(HttpStatus.BAD_REQUEST, "GAME-010", "첫 결과 제출에는 리뷰가 필수입니다."),
    GAME_RESULT_SUBMISSION_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "GAME-011", "결과 제출은 유저당 최대 2회까지만 가능합니다."),
    GAME_RESULT_NOT_WAITING_CONFIRMATION(HttpStatus.BAD_REQUEST, "GAME-012", "현재 게임 상태에서는 결과를 확정할 수 없습니다."),
    GAME_SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "GAME-013", "경기 결과 제출안을 찾을 수 없습니다."),
    GAME_SUBMISSION_NOT_SUBMITTED(HttpStatus.BAD_REQUEST, "GAME-014", "이미 처리된 제출안입니다."),
    GAME_SUBMISSION_CONFIRMER_MISMATCH(HttpStatus.FORBIDDEN, "GAME-015", "해당 제출안을 확정할 권한이 없습니다."),

    // Domain - Sport
    SPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "SPORT-001", "존재하지 않는 스포츠 코드입니다."),

    // Domain - Tier
    TIER_NOT_FOUND(HttpStatus.NOT_FOUND, "TIER-001", "LP에 해당하는 티어 정보를 찾을 수 없습니다."),
    INVALID_INITIAL_TIER(HttpStatus.BAD_REQUEST, "TIER-002", "초기 티어로 설정할 수 없는 등급입니다."),
    INVALID_TIER_SETTING(HttpStatus.BAD_REQUEST, "TIER-003", "해당 종목에 존재하지 않는 티어 이름입니다."),

    // Domain - Notification
    NOTIFICATION_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTI-001", "알림 템플릿을 찾을 수 없습니다."),

}
