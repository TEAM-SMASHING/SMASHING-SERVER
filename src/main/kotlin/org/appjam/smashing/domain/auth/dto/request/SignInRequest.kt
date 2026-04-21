package org.appjam.smashing.domain.auth.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.auth.dto.command.SignInRequestCommand
import org.appjam.smashing.domain.auth.enums.ProviderType
import org.appjam.smashing.global.common.validator.annotation.ValidEnum
import org.appjam.smashing.global.extensions.ofIgnoreCase

data class SignInRequest(
    @field:NotBlank(message = "idToken을 입력해주세요.")
    val idToken: String?,
    @field:NotBlank(message = "provider를 입력해주세요.")
    @field:ValidEnum(message = "잘못된 provider 값입니다.", enumClass = ProviderType::class)
    val provider: String?,
) {
    fun toCommand() = SignInRequestCommand(
        idToken = idToken!!,
        provider = ofIgnoreCase<ProviderType>(provider!!),
    )
}
