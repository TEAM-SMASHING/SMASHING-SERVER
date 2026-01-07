package org.appjam.smashing.domain.matching.repository

import org.appjam.smashing.domain.matching.entity.Matching
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface MatchingRepository : JpaRepository<Matching, String> {

    @Query(
        value = """
        select count(*)
          from matching m
         where (
                (m.requester_user_id = :userA and m.receiver_user_id = :userB)
             or (m.requester_user_id = :userB and m.receiver_user_id = :userA)
         )
           and m.created_at >= :startAt
        """,
        nativeQuery = true
    )
    fun countTodayBetweenUsersIncludingDeleted(
        userA: String,
        userB: String,
        startAt: LocalDateTime,
    ): Long

    @Query(
        """
        select m
          from Matching m
          join fetch m.requester
          join fetch m.receiver
          join fetch m.sport
         where m.id = :matchingId
        """
    )
    fun findByIdFetchAll(
        matchingId: String
    ): Matching?
}
