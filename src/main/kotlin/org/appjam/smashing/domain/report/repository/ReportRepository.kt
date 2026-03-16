package org.appjam.smashing.domain.report.repository

import org.appjam.smashing.domain.report.entity.Report
import org.appjam.smashing.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ReportRepository : JpaRepository<Report, String> {
    fun existsByReporterAndReportedUser(
        reporter: User,
        reportedUser: User,
    ): Boolean

    @Query(
        """
        SELECT COUNT(DISTINCT r.reporter) 
        FROM Report r 
        WHERE r.reportedUser = :reportedUser 
        AND r.createdAt >= :since
    """
    )
    fun countRecentReports(
        @Param("reportedUser") reportedUser: User,
        @Param("since") since: LocalDateTime
    ): Long
}
