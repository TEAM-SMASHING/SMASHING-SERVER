package org.appjam.smashing.domain.outbox.enums

enum class SseEventType(
    val eventName: String
) {

    // 받은 매칭 요청 카드 실시간 추가
    MATCHING_RECEIVED("matching.received"),

    // 보낸 매칭 요청 카드 실시간 추가
    MATCHING_SENT("matching.sent"),

    // 매칭 엔티티 상태 업데이트 실시간 반영 (삭제(취소))
    MATCHING_UPDATED("matching.updated"),


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
