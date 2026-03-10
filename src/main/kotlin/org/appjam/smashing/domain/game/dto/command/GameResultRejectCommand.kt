package org.appjam.smashing.domain.game.dto.command

import org.appjam.smashing.domain.game.enums.GameSubmissionRejectReason

data class GameResultRejectCommand(
    val reason: GameSubmissionRejectReason?,
)
