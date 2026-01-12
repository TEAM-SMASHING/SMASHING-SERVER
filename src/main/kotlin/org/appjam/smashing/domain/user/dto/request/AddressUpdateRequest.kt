package org.appjam.smashing.domain.user.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.user.dto.command.AddressUpdateCommand

data class AddressUpdateRequest(
    @field:NotBlank(message = "region을 입력해주세요.")
    val region: String?,
) {
    fun toCommand() = AddressUpdateCommand(
        region = region!!,
    )
}
