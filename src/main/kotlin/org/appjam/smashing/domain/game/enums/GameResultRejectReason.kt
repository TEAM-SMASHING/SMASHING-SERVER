package org.appjam.smashing.domain.game.enums

enum class GameResultRejectReason {
    SCORE_MISMATCH,               // 점수 오류
    WIN_LOSE_REVERSED,            // 승패 반전
    SCORE_AND_WIN_LOSE_MISMATCH,  // 승패 반전 + 점수 오류
    GAME_NOT_PLAYED_YET,          // 아직 진행되지 않은 경기
}
