package org.appjam.smashing.domain.outbox.enums

enum class SseEventType(
    val eventName: String
) {
    // 내가 요청 받음
    MATCHING_RECEIVED("matching.received"),

    // 상대가 요청을 수락/거절/삭제
    MATCHING_UPDATED("matching.updated"),

    // 알림 관련 생성 / 변경 사항
    NOTIFICATION_CREATED("notification.created"),

    // 매칭 요청 알림 생성
    MATCHING_REQUEST_NOTIFICATION_CREATED("matching.request.notification.created"),
}
