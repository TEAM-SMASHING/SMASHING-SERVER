package org.appjam.smashing.domain.user.dto.command

import org.appjam.smashing.domain.user.enums.ReportType

data class UserReportCommand(
    val reportedUserProfileId: String,
    val reportType: ReportType,
    val reasonDetail: String?,
)
