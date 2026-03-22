package org.appjam.smashing.domain.user.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.user.dto.command.UserBlockCommand

data class UserBlockRequest(
    @field:NotBlank(message = "blockedUserProfileId 값은 필수입니다.")
    val blockedUserProfileId: String?,
) {
    fun toCommand() = UserBlockCommand(
        blockedUserProfileId = blockedUserProfileId!!
    )
}
