package org.appjam.smashing.domain.game.enums

enum class GameResultStatus {
    PENDING_RESULT,           // 경기 결과 대기 중
    WAITING_CONFIRMATION,     // 경기 결과 확인 대기 중
    RESULT_CONFIRMED,         // 경기 결과 확인 완료
    RESULT_REJECTED,          // 경기 결과 거부됨
    CANCELED                  // 경기 취소됨
}
