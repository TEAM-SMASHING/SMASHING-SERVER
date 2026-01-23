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

    @Modifying
    @Query(
        """
    update Matching m
       set m.deletedAt = :deletedAt
     where m.deletedAt is null
       and m.status = :status
       and m.id <> :excludeMatchingId
       and m.sport.id = :sportId
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
        sportId: Long,
    ): Int

    fun findFirstByReceiverIdAndRequesterIdAndSportIdAndStatusOrderByCreatedAtDesc(
        receiverId: String,
        requesterId: String,
        sportId: Long,
        status: MatchingStatus
    ): Matching?

    @Query(
        value = """
     select exists(
        select 1
        from matching m
        where m.created_at >= :startAt
          and m.status not in ('ACCEPTED', 'COMPLETED')
          and m.requester_user_id = :requesterUserId
          and m.receiver_user_id = :receiverUserId
    )
    """,
        nativeQuery = true
    )
    fun existsPendingRequestFromRequesterToReceiverSinceRaw(
        startAt: LocalDateTime,
        requesterUserId: String,
        receiverUserId: String,
    ): Long

    @Query(
        value = """
        select exists(
            select 1
              from matching m
              left join game g
                     on g.matching_id = m.id
             where m.created_at >= :startAt
               and m.requester_user_id = :requesterUserId
               and m.receiver_user_id = :receiverUserId
               and (g.id is null or g.result_status <> 'RESULT_CONFIRMED')
        )
    """,
        nativeQuery = true
    )
    fun existsUnconfirmedOutgoingMatchingSinceRaw(
        startAt: LocalDateTime,
        requesterUserId: String,
        receiverUserId: String,
    ): Long

    @Query(
        value = """
    select exists(
        select 1
          from matching m
          left join game g
                 on g.matching_id = m.id
         where m.created_at >= :startAt
           and (
                (m.requester_user_id = :userA and m.receiver_user_id = :userB)
             or (m.requester_user_id = :userB and m.receiver_user_id = :userA)
           )
           and (g.id is null or g.result_status <> 'RESULT_CONFIRMED')
    )
    """,
        nativeQuery = true
    )
    fun existsUnconfirmedMatchingBetweenUsersSinceRaw(
        startAt: LocalDateTime,
        userA: String,
        userB: String,
    ): Long
}
