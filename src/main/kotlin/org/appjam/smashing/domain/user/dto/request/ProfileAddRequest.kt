package org.appjam.smashing.domain.user.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.user.dto.command.ProfileAddCommand

data class ProfileAddRequest(
    @field:NotBlank(message = "sportCode를 입력해주세요.")
    val sportCode: String?,
    @field:NotBlank(message = "experienceRange를 입력해주세요.")
    val experienceRange: String?,
) {
    fun toCommand() = ProfileAddCommand(
        sportCode = sportCode!!,
        experienceRange = experienceRange!!,
    )
}
