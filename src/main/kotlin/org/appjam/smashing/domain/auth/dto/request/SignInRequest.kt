package org.appjam.smashing.domain.auth.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.auth.dto.command.SignInRequestCommand

data class SignInRequest(
    @field:NotBlank(message = "엑세스 토큰을 입력해주세요.")
    val accessToken: String?,
) {
    fun toCommand(): SignInRequestCommand =
        SignInRequestCommand(
            accessToken = accessToken!!,
        )
}
