package org.appjam.smashing.domain.report.dto.command

import org.appjam.smashing.domain.report.enums.ReportType

data class UserReportCommand(
    val reportedUserId: String,
    val reportType: ReportType,
    val reasonDetail: String?,
)
