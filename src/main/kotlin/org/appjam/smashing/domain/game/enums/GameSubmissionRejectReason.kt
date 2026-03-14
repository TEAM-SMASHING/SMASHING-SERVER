package org.appjam.smashing.domain.game.enums

enum class GameSubmissionRejectReason {
    WINNER_MISMATCH,     // 승자가 잘못됐어요
    GAME_NOT_PLAYED_YET, // 아직 진행하지 않은 경기에요
}
