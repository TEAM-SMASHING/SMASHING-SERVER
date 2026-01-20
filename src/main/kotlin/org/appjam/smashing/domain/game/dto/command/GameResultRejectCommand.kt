package org.appjam.smashing.domain.game.dto.command

import org.appjam.smashing.domain.game.enums.GameResultRejectReason

data class GameResultRejectCommand(
    val reason: GameResultRejectReason?,
)
