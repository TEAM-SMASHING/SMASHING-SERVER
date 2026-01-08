package org.appjam.smashing.domain.auth.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.auth.command.SignUpRequestCommand

data class SignUpRequest(
    @field:NotBlank(message = "nickname을 입력해주세요.")
    val nickname: String?,
    @field:NotBlank(message = "gender를 입력해주세요.")
    val gender: String?,
    @field:NotBlank(message = "openChatUrl을 입력해주세요.")
    val openChatUrl: String?,
    @field:NotBlank(message = "sportCode를 입력해주세요.")
    val sportCode: String?,
    @field:NotBlank(message = "tier를 입력해주세요.")
    val tier: String?,
    @field:NotBlank(message = "region을 입력해주세요.")
    val region: String?,
) {
    fun toCommand(): SignUpRequestCommand = SignUpRequestCommand(
        nickname = nickname!!,
        gender = gender!!,
        openChatUrl = openChatUrl!!,
        sportCode = sportCode!!,
        tier = tier!!,
        region = region!!,
    )
}
