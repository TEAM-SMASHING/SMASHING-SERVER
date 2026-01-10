package org.appjam.smashing.domain.matching.repository

import jakarta.persistence.LockModeType
import org.appjam.smashing.domain.matching.entity.Matching
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface MatchingRepository : JpaRepository<Matching, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select m
        from Matching m
        join fetch m.requester
        join fetch m.receiver
        join fetch m.sport
        where m.id = :matchingId
          and m.deletedAt is null
        """
    )
    fun findByIdFetchAllForUpdate(
        matchingId: String
    ): Matching?
}
