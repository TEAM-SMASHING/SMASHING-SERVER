package org.appjam.smashing.domain.game.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.game.dto.command.GameResultRejectCommand
import org.appjam.smashing.domain.game.enums.GameResultRejectReason
import org.appjam.smashing.global.common.validator.annotation.ValidEnum
import org.appjam.smashing.global.extensions.ofIgnoreCase

data class GameResultRejectRequest(
    @field:NotBlank(message = "reason은 필수입니다.")
    @field:ValidEnum(message = "잘못된 reason 값입니다.", enumClass = GameResultRejectReason::class)
    val reason: String?,
) {
    fun toCommand() = GameResultRejectCommand(
        reason = ofIgnoreCase<GameResultRejectReason>(reason!!),
    )
}
