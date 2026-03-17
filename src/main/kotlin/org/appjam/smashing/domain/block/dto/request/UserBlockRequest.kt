package org.appjam.smashing.domain.block.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.block.dto.command.UserBlockCommand

data class UserBlockRequest(
    @field:NotBlank(message = "blockedUser를 입력해주세요.")
    val blockedUser: String?,
) {
    fun toCommand() = UserBlockCommand(
        blockedUser = blockedUser!!
    )
}
