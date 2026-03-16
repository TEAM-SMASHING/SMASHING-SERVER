package org.appjam.smashing.domain.report.repository

import org.appjam.smashing.domain.report.entity.Report
import org.springframework.data.jpa.repository.JpaRepository

interface ReportRepository : JpaRepository<Report, String>
