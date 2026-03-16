package org.appjam.smashing.domain.report.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.report.dto.command.UserReportCommand

data class UserReportRequest(
    @field:NotBlank(message = "reportedUserId를 입력해주세요.")
    val reportedUserId: String?,
    @field:NotBlank(message = "reportType을 입력해주세요.")
    val reportType: String?,
    val reasonDetail: String?
) {
    fun toCommand() = UserReportCommand(
        reportedUserId = reportedUserId!!,
        reportType = reportType!!,
        reasonDetail = reasonDetail,
    )
}
