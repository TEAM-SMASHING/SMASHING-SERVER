package org.appjam.smashing.domain.sport.repository

import org.appjam.smashing.domain.sport.entity.Tier
import org.springframework.data.jpa.repository.JpaRepository

interface TierRepository : JpaRepository<Tier, Long>
