package org.appjam.smashing.domain.matching.repository

import jakarta.persistence.LockModeType
import org.appjam.smashing.domain.matching.entity.Matching
import org.appjam.smashing.domain.matching.enums.MatchingStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface MatchingRepository : JpaRepository<Matching, String>, MatchingRepositoryCustom {

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

    @Query(
        """
        select case when count(m) > 0 then true else false end
        from Matching m
        where m.status = :status
          and m.createdAt >= :startAt
          and (
                (m.requester.id = :userA and m.receiver.id = :userB)
             or (m.requester.id = :userB and m.receiver.id = :userA)
          )
        """
    )
    fun existsBetweenUsersSinceWithStatus(
        startAt: LocalDateTime,
        userA: String,
        userB: String,
        status: MatchingStatus,
    ): Boolean

    @Modifying
    @Query(
        """
        update Matching m
           set m.deletedAt = :deletedAt
         where m.deletedAt is null
           and m.status = :status
           and m.id <> :excludeMatchingId
           and (
                (m.requester.id = :userA and m.receiver.id = :userB)
             or (m.requester.id = :userB and m.receiver.id = :userA)
           )
        """
    )
    fun softDeleteRequestedBetweenUsersExcept(
        deletedAt: LocalDateTime,
        status: MatchingStatus,
        excludeMatchingId: String,
        userA: String,
        userB: String,
    ): Int

    fun findFirstByReceiverIdAndSportIdAndStatusOrderByCreatedAtDesc(
        receiverId: String,
        sportId: Long,
        status: MatchingStatus
    ): Matching?
}
