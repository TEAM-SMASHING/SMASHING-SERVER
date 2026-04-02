package org.appjam.smashing.domain.user.dto.request

import jakarta.validation.constraints.NotBlank
import org.appjam.smashing.domain.user.dto.command.UserReportCommand
import org.appjam.smashing.domain.user.enums.ReportType
import org.appjam.smashing.global.common.validator.annotation.ValidEnum
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.appjam.smashing.global.extensions.ofIgnoreCase

data class UserReportRequest(
    @field:NotBlank(message = "reportedUserProfileId 입력해주세요.")
    val reportedUserProfileId: String?,

    @field:NotBlank(message = "reportType을 입력해주세요.")
    @field:ValidEnum(message = "잘못된 reportType 값입니다.", enumClass = ReportType::class)
    val reportType: String?,

    val reasonDetail: String?
) {
    fun toCommand(): UserReportCommand {
        val type = ofIgnoreCase<ReportType>(reportType!!)

        if (type == ReportType.ETC && reasonDetail.isNullOrBlank()) {
            throw CustomException(ErrorCode.REPORT_REASON_REQUIRED)
        }

        if (type != ReportType.ETC && !reasonDetail.isNullOrBlank()) {
            throw CustomException(ErrorCode.REPORT_REASON_NOT_ALLOWED)
        }

        return UserReportCommand(
            reportedUserProfileId = reportedUserProfileId!!,
            reportType = type,
            reasonDetail = reasonDetail,
        )
    }
}
