package org.appjam.smashing.domain.report.service

import org.appjam.smashing.domain.report.dto.command.UserReportCommand
import org.appjam.smashing.domain.report.entity.Report
import org.appjam.smashing.domain.report.enums.ReportType
import org.appjam.smashing.domain.report.repository.ReportRepository
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.repository.UserRepository
import org.appjam.smashing.domain.user.repository.UserSportProfileRepository
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReportService(
    private val userRepository: UserRepository,
    private val userSportProfileRepository: UserSportProfileRepository,
    private val reportRepository: ReportRepository,
) {
    @Transactional
    fun reportUser(
        userId: String,
        requestCommand: UserReportCommand,
    ) {
        if (requestCommand.reportType == ReportType.ETC && requestCommand.reasonDetail.isNullOrBlank()) {
            throw CustomException(ErrorCode.REPORT_REASON_REQUIRED)
        }

        val reporter = userRepository.findByIdOrNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        val reportedUserProfile = userSportProfileRepository.findByIdOrNull(requestCommand.reportedUserProfileId)
            ?: throw CustomException(ErrorCode.REPORTED_PROFILE_NOT_FOUND)

        // 조치1 - 자기 자신 신고 방지
        if (userId == reportedUserProfile.user.id) {
            throw CustomException(ErrorCode.REPORT_SELF_FORBIDDEN)
        }

        val thirtyDaysAgo = LocalDateTime.now().minusDays(30)

        // 조치2 - 비관적 락으로 중복 신고 확인 (30일이 지나면 동일 유저에게 신고 가능)
        val recentReport = reportRepository.findRecentReportWithLock(
            reporter = reporter,
            reportedUser = reportedUserProfile.user,
            since = thirtyDaysAgo,
        )
        if (recentReport.isNotEmpty()) {
            throw CustomException(ErrorCode.REPORT_ALREADY_EXISTS)
        }

        // 정책1 - 신고 데이터 저장
        val report = Report.create(
            reporter = reporter,
            reportedUser = reportedUserProfile.user,
            reportType = requestCommand.reportType,
            reasonDetail = requestCommand.reasonDetail,
        )
        reportRepository.save(report)

        // 정책2 - 자동 제재 정책 확인
        checkAndApplyRestriction(reportedUserProfile.user)
    }

    private fun checkAndApplyRestriction(user: User) {
        // 제재 종료일이 미래라면 이미 제재가 적용됐으므로 return
        val now = LocalDateTime.now()
        if (user.restrictionEndDate?.isAfter(now) == true) return

        // 30일 내에 서로 다른 신고자에게 받은 신고 횟수 카운트
        val thirtyDaysAgo = LocalDateTime.now().minusDays(30)
        val reportCount = reportRepository.countRecentReports(
            reportedUser = user,
            since = thirtyDaysAgo,
        )

        if (reportCount >= 3) {
            // 유저 상태 변경 및 제한 종료일 설정 (7일)
            user.applyRestriction(durationDays = 7)

            // TODO:  예정된 경기 및 매칭 요청 일괄 무효 처리
        }
    }
}
