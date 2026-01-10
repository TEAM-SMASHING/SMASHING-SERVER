package org.appjam.smashing.domain.user.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.user.command.OpenChatValidateCommand

data class OpenChatValidateRequest(
    @field:NotBlank(message = "openchatUrl을 입력해주세요.")
    val openchatUrl: String?,
) {
    fun toCommand(): OpenChatValidateCommand = OpenChatValidateCommand(
        openchatUrl = openchatUrl!!,
    )
}
