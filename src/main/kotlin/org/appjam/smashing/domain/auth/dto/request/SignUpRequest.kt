package org.appjam.smashing.domain.auth.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.auth.command.SignUpRequestCommand
import org.appjam.smashing.domain.sport.enums.SportCode
import org.appjam.smashing.domain.sport.enums.TierType
import org.appjam.smashing.global.common.validator.annotation.ValidEnum
import org.appjam.smashing.global.extensions.ofIgnoreCase

data class SignUpRequest(
    @field:NotBlank(message = "authId를 입력해주세요.")
    val authId: String?,
    @field:NotBlank(message = "nickname을 입력해주세요.")
    val nickname: String?,
    @field:NotBlank(message = "gender를 입력해주세요.")
    val gender: String?,
    @field:NotBlank(message = "openChatUrl을 입력해주세요.")
    val openChatUrl: String?,
    @field:NotBlank(message = "sportCode를 입력해주세요.")
    @field:ValidEnum(message = "잘못된 sportCode 값입니다.", enumClass = SportCode::class)
    val sportCode: String?,
    @field:NotBlank(message = "tier를 입력해주세요.")
    @field:ValidEnum(message = "잘못된 tier 값입니다.", enumClass = TierType::class)
    val tier: String?,
    @field:NotBlank(message = "region을 입력해주세요.")
    val region: String?,
) {
    fun toCommand(): SignUpRequestCommand = SignUpRequestCommand(
        authId = authId!!,
        nickname = nickname!!,
        gender = gender!!,
        openChatUrl = openChatUrl!!,
        sportCode = ofIgnoreCase<SportCode>(sportCode!!),
        tier = ofIgnoreCase<TierType>(tier!!),
        region = region!!,
    )
}
