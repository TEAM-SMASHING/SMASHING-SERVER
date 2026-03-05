package org.appjam.smashing.domain.outbox.enums

enum class SseEventType(
    val eventName: String
) {

    // 받은 매칭 요청 카드 실시간 추가
    MATCHING_RECEIVED("matching.received"),

    // 보낸 매칭 요청 카드 실시간 추가
    MATCHING_SENT("matching.sent"),


    // 상대가 요청을 수락/거절/삭제
    MATCHING_UPDATED("matching.updated"),

    // 알림 관련 생성 / 변경 사항
    NOTIFICATION_CREATED("notification.created"),

    // 매칭 요청 알림 생성
    MATCHING_REQUEST_NOTIFICATION_CREATED("matching.request.notification.created"),

    // 매칭 수락 알림 생성
    MATCHING_ACCEPT_NOTIFICATION_CREATED("matching.accept.notification.created"),

    // 게임 관련 변경 사항
    GAME_UPDATED("game.updated"),

    // 게임 결과 제출 알림 생성
    GAME_RESULT_SUBMITTED_NOTIFICATION_CREATED("game.result.submitted.notification.created"),

    // 게임 결과 거절 알림 생성
    GAME_RESULT_REJECTED_NOTIFICATION_CREATED("game.result.rejected.notification.created"),

    // 리뷰 제출 알림 생성
    REVIEW_RECEIVED_NOTIFICATION_CREATED("review.received.notification.created"),
}
