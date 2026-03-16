package org.appjam.smashing.domain.report.dto.command

data class UserReportCommand(
    val reportedUserId: String,
    val reportType: String,
    val reasonDetail: String?,
)
