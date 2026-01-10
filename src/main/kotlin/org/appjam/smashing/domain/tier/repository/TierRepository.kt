package org.appjam.smashing.domain.tier.repository

import org.appjam.smashing.domain.tier.entity.Tier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TierRepository : JpaRepository<Tier, Long> {

    @Query(
        """
        select t
          from Tier t
         where t.sport.id = :sportId
           and :lp between t.minLp and t.maxLp
        """
    )
    fun findBySportIdAndLpInRange(
        sportId: Long,
        lp: Int,
    ): Tier?

    fun findBySportIdAndName(
        sportId: Long,
        name: String,
    ): Tier?
}
