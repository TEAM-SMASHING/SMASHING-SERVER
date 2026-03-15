package org.appjam.smashing.domain.matching.repository

import jakarta.persistence.LockModeType
import org.appjam.smashing.domain.matching.dto.projection.LatestMatchingCooldownProjection
import org.appjam.smashing.domain.matching.entity.Matching
import org.appjam.smashing.domain.matching.enums.MatchingStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MatchingRepository : JpaRepository<Matching, String>, MatchingRepositoryCustom {

    /**
     * 마지막 매칭(soft delete 포함) 1건 조회 (쿨다운 판단용)
     * - cancelled/rejected는 responded_at 기준으로 24h
     * - requested 상태는 created_at 기준으로 24h
     */
    @Query(
        value = """
        select 
            m.status as status,
            m.created_at as createdAt,
            m.responded_at as respondedAt
        from matching m
        where m.sport_id = :sportId
          and (
                (m.requester_profile_id = :profileA and m.receiver_profile_id = :profileB)
             or (m.requester_profile_id = :profileB and m.receiver_profile_id = :profileA)
          )
        order by m.created_at desc
        limit 1
        """,
        nativeQuery = true
    )
    fun findLatestForCooldown(
        @Param("profileA") profileA: String,
        @Param("profileB") profileB: String,
        @Param("sportId") sportId: Long,
    ): LatestMatchingCooldownProjection?

    /**
     * 매칭 row에 대해 락을 걸고 조회 (상태 전환이 한번만 일어나도록)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select m
          from Matching m
          join fetch m.requesterProfile rp
          join fetch rp.user rpu
          join fetch m.receiverProfile rcp
          join fetch rcp.user rcu
          join fetch m.sport s
         where m.id = :matchingId
        """
    )
    fun findByIdFetchAllForUpdate(
        matchingId: String,
    ): Matching?

//    @Modifying
//    @Query(
//        """
//    update Matching m
//       set m.deletedAt = :deletedAt
//     where m.deletedAt is null
//       and m.status = :status
//       and m.id <> :excludeMatchingId
//       and m.sport.id = :sportId
//       and (
//            (m.requester.id = :userA and m.receiver.id = :userB)
//         or (m.requester.id = :userB and m.receiver.id = :userA)
//       )
//    """
//    )
//    fun softDeleteRequestedBetweenUsersExcept(
//        deletedAt: LocalDateTime,
//        status: MatchingStatus,
//        excludeMatchingId: String,
//        userA: String,
//        userB: String,
//        sportId: Long,
//    ): Int

    fun findFirstByReceiverProfileIdAndRequesterProfileIdAndSportIdAndStatusOrderByCreatedAtDesc(
        receiverProfileId: String,
        requesterProfileId: String,
        sportId: Long,
        status: MatchingStatus
    ): Matching?

//    @Query(
//        value = """
//    select exists(
//        select 1
//          from matching m
//          left join game g
//                 on g.matching_id = m.id
//         where m.created_at >= :startAt
//           and (
//                (m.requester_user_id = :userA and m.receiver_user_id = :userB)
//             or (m.requester_user_id = :userB and m.receiver_user_id = :userA)
//           )
//           and (g.id is null or g.result_status <> 'RESULT_CONFIRMED')
//    )
//    """,
//        nativeQuery = true
//    )
//    fun existsUnconfirmedMatchingBetweenUsersSinceRaw(
//        startAt: LocalDateTime,
//        userA: String,
//        userB: String,
//    ): Long
}
