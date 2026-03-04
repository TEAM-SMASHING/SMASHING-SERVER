package org.appjam.smashing.domain.auth.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.auth.dto.command.TokenReissueCommand

data class TokenReissueRequest(
    @field:NotBlank(message = "refreshToken을 입력해주세요.")
    val refreshToken: String,
) {
    fun toCommand() = TokenReissueCommand(
        refreshToken = refreshToken,
    )
}
