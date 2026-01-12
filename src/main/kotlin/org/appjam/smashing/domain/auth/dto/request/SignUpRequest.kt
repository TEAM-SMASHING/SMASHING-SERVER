package org.appjam.smashing.domain.auth.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.auth.dto.command.SignUpRequestCommand
import org.appjam.smashing.domain.user.enums.Gender
import org.appjam.smashing.global.common.validator.annotation.ValidEnum
import org.appjam.smashing.global.extensions.ofIgnoreCase

data class SignUpRequest(
    @field:NotBlank(message = "authId를 입력해주세요.")
    val authId: String?,
    @field:NotBlank(message = "nickname을 입력해주세요.")
    val nickname: String?,
    @field:NotBlank(message = "gender를 입력해주세요.")
    @field:ValidEnum(message = "잘못된 gender 값입니다.", enumClass = Gender::class)
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
    fun toCommand() = SignUpRequestCommand(
        authId = authId!!,
        nickname = nickname!!,
        gender = ofIgnoreCase<Gender>(gender!!),
        openChatUrl = openChatUrl!!,
        sportCode = sportCode!!,
        tier = tier!!,
        region = region!!,
    )
}
