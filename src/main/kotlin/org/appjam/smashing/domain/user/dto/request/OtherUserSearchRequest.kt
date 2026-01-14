package org.appjam.smashing.domain.user.dto.request

import jakarta.validation.constraints.Size
import org.appjam.smashing.domain.user.dto.command.OtherUserSearchCommand

data class OtherUserSearchRequest(
    @field:Size(max = 10, message = "nickname은 10자 이하입니다.")
    val nickname: String?
) {
    fun toCommand() = OtherUserSearchCommand(
        nickname = nickname!!,
    )
}
