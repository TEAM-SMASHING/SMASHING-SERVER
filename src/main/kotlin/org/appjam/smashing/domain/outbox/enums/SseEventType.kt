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

    // 게임 관련 변경 사항
    GAME_UPDATED("game.updated"),
}
