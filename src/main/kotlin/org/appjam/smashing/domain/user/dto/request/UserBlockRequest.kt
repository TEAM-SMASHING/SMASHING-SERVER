package org.appjam.smashing.domain.user.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.user.dto.command.UserBlockCommand

data class UserBlockRequest(
    @field:NotBlank(message = "blockedUserProfileId를 입력해주세요.")
    val blockedUserProfileId: String?,
) {
    fun toCommand() = UserBlockCommand(
        blockedUserProfileId = blockedUserProfileId!!
    )
}
