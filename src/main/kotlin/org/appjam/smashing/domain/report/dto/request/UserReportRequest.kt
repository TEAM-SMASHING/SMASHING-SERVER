package org.appjam.smashing.domain.report.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.report.dto.command.UserReportCommand
import org.appjam.smashing.domain.report.enums.ReportType
import org.appjam.smashing.global.common.validator.annotation.ValidEnum
import org.appjam.smashing.global.extensions.ofIgnoreCase

data class UserReportRequest(
    @field:NotBlank(message = "reportedUserProfileId 입력해주세요.")
    val reportedUserProfileId: String?,
    @field:NotBlank(message = "reportType을 입력해주세요.")
    @field:ValidEnum(message = "잘못된 reportType 값입니다.", enumClass = ReportType::class)
    val reportType: String?,
    val reasonDetail: String?
) {
    fun toCommand() = UserReportCommand(
        reportedUserProfileId = reportedUserProfileId!!,
        reportType = ofIgnoreCase<ReportType>(reportType!!),
        reasonDetail = reasonDetail,
    )
}
