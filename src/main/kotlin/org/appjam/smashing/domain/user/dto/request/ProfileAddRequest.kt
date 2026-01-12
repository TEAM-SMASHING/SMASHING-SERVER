package org.appjam.smashing.domain.user.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.user.command.ProfileAddCommand

data class ProfileAddRequest(
    @field:NotBlank(message = "sportCode를 입력해주세요.")
    val sportCode: String?,
    @field:NotBlank(message = "tier를 입력해주세요.")
    val tier: String?,
) {
    fun toCommand() = ProfileAddCommand(
        sportCode = sportCode!!,
        tier = tier!!,
    )
}
