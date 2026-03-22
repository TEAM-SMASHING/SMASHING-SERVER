package org.appjam.smashing.domain.report.dto.command

import org.appjam.smashing.domain.report.enums.ReportType

data class UserReportCommand(
    val reportedUserProfileId: String,
    val reportType: ReportType,
    val reasonDetail: String?,
)
