package org.appjam.smashing.domain.game.dto.request

import org.appjam.smashing.domain.game.dto.command.GameResultRejectCommand
import org.appjam.smashing.domain.game.enums.GameResultRejectReason
import org.appjam.smashing.global.common.validator.annotation.ValidEnum
import org.appjam.smashing.global.extensions.ofIgnoreCaseOrNull

data class GameResultRejectRequest(
    @field:ValidEnum(message = "잘못된 reason 값입니다.", enumClass = GameResultRejectReason::class)
    val reason: String? = null,
) {
    fun toCommand() = GameResultRejectCommand(
        reason = ofIgnoreCaseOrNull<GameResultRejectReason>(reason),
    )
}
