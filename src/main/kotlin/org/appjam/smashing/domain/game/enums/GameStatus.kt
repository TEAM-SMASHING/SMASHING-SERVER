package org.appjam.smashing.domain.game.enums

enum class GameStatus {
    PENDING_RESULT,           // 경기 결과 대기 중
    WAITING_CONFIRMATION,     // 경기 결과 확인 대기 중
    RESULT_CONFIRMED,         // 경기 결과 확인 완료
    RESULT_REJECTED,          // 경기 결과 반려됨
    CANCELED                  // 경기 취소됨
}
