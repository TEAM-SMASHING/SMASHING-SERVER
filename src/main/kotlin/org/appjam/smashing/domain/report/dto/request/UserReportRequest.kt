package org.appjam.smashing.domain.report.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.report.dto.command.UserReportCommand

data class UserReportRequest(
    @field:NotBlank(message = "userId를 입력해주세요.")
    val userId: String?,
) {
    fun toCommand() = UserReportCommand(
        userId = userId!!,
    )
}
