package org.appjam.smashing.domain.lp.repository

import org.appjam.smashing.domain.lp.entity.LpHistory
import org.springframework.data.jpa.repository.JpaRepository

interface LpHistoryRepository : JpaRepository<LpHistory, String>
