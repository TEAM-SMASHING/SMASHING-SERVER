package org.appjam.smashing.domain.user.repository

import jakarta.persistence.LockModeType
import org.appjam.smashing.domain.user.entity.Report
import org.appjam.smashing.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface ReportRepository : JpaRepository<Report, String> {
    @Query(
        """
        SELECT COUNT(DISTINCT r.reporter) 
        FROM Report r 
        WHERE r.reportedUser = :reportedUser 
        AND r.createdAt >= :since
    """
    )
    fun countRecentReports(
        reportedUser: User,
        since: LocalDateTime
    ): Long

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
          SELECT r 
          FROM Report r 
          WHERE r.reporter = :reporter
          AND r.reportedUser = :reportedUser 
          AND r.createdAt >= :since
     """
    )
    fun findRecentReportWithLock(
        reporter: User,
        reportedUser: User,
        since: LocalDateTime,
    ): List<Report>
}
