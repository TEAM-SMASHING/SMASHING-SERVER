package org.appjam.smashing.domain.game.repository

import jakarta.persistence.LockModeType
import org.appjam.smashing.domain.game.entity.Game
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface GameRepository : JpaRepository<Game, String>, GameRepositoryCustom {

    /**
     * 하루 3판 제한(종목별 동일 상대) 판단용
     * - 오늘 RESULT_CONFIRMED 된 게임 수 카운트 (양방향 합산)
     */
    @Query(
        value = """
        select count(*)
        from game g
        join matching m on m.id = g.matching_id
        where g.sport_id = :sportId
          and g.result_status = 'RESULT_CONFIRMED'
          and g.confirmed_at >= :startOfDay
          and g.confirmed_at < :endOfDay
          and (
                (m.requester_profile_id = :profileA and m.receiver_profile_id = :profileB)
             or (m.requester_profile_id = :profileB and m.receiver_profile_id = :profileA)
          )
        """,
        nativeQuery = true
    )
    fun countConfirmedGamesTodayBetweenProfiles(
        @Param("profileA") profileA: String,
        @Param("profileB") profileB: String,
        @Param("sportId") sportId: Long,
        @Param("startOfDay") startOfDay: LocalDateTime,
        @Param("endOfDay") endOfDay: LocalDateTime,
    ): Long

    fun existsByMatchingId(
        matchingId: String
    ): Boolean

    @Query(
        value = """
        select count(*)
        from game g
        join matching m on m.id = g.matching_id
        where g.result_status = 'RESULT_CONFIRMED'
          and g.confirmed_at >= :startAt
          and (
                (m.requester_profile_id = :profileA and m.receiver_profile_id = :profileB)
             or (m.requester_profile_id = :profileB and m.receiver_profile_id = :profileA)
          )
        """,
        nativeQuery = true
    )
    fun countTodayConfirmedGamesBetweenProfiles(
        @Param("startAt") startAt: LocalDateTime,
        @Param("profileA") profileA: String,
        @Param("profileB") profileB: String,
    ): Long

    @Query(
        value = """
        select g.confirmed_at
        from game g
        join matching m on m.id = g.matching_id
        where g.result_status = 'RESULT_CONFIRMED'
          and g.confirmed_at >= :startAt
          and (
                (m.requester_profile_id = :profileA and m.receiver_profile_id = :profileB)
             or (m.requester_profile_id = :profileB and m.receiver_profile_id = :profileA)
          )
        order by g.confirmed_at desc
        limit 1
        """,
        nativeQuery = true
    )
    fun findTodayLatestConfirmedAtBetweenProfiles(
        @Param("startAt") startAt: LocalDateTime,
        @Param("profileA") profileA: String,
        @Param("profileB") profileB: String,
    ): LocalDateTime?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select g
          from Game g
          join fetch g.matching m
          join fetch m.requesterProfile rp
          join fetch rp.user
          join fetch rp.tier
          join fetch m.receiverProfile rcp
          join fetch rcp.user
          join fetch rcp.tier
          join fetch g.sport
         where g.id = :gameId
        """
    )
    fun findByIdFetchAllForUpdate(
        @Param("gameId") gameId: String,
    ): Game?

    @Query(
        value = """
            select count(*)
            from game g
            join matching m on m.id = g.matching_id
            where g.result_status = 'RESULT_CONFIRMED'
            and g.confirmed_at >= :startAt
            and (
            (m.requester_user_id = :userA and m.receiver_user_id = :userB)
         or (m.requester_user_id = :userB and m.receiver_user_id = :userA)
          )
      """,
        nativeQuery = true
    )
    fun countTodayConfirmedGamesBetweenUsers(
        @Param("startAt") startAt: LocalDateTime,
        @Param("userA") userA: String,
        @Param("userB") userB: String,
    ): Long

    @Query(
        value = """
            select g.confirmed_at
              from game g
              join matching m on m.id = g.matching_id
             where g.result_status = 'RESULT_CONFIRMED'
               and g.confirmed_at >= :startAt
               and (
                    (m.requester_user_id = :userA and m.receiver_user_id = :userB)
                 or (m.requester_user_id = :userB and m.receiver_user_id = :userA)
               )
             order by g.confirmed_at desc
             limit 1
        """,
        nativeQuery = true
    )
    fun findTodayLatestConfirmedAtBetweenUsers(
        startAt: LocalDateTime,
        userA: String,
        userB: String,
    ): LocalDateTime?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select g
        from Game g
        where g.id = :gameId
        """
    )
    fun findByIdForUpdate(
        gameId: String,
    ): Game?
//
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query(
//        """
//        select g
//          from Game g
//          join fetch g.matching m
//          join fetch m.requester
//          join fetch m.receiver
//         where g.id = :gameId
//        """
//    )
//    fun findByIdFetchUsersForUpdate(gameId: String): Game?
}
