package org.appjam.smashing.domain.game.repository

import jakarta.persistence.LockModeType
import org.appjam.smashing.domain.game.entity.Game
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface GameRepository : JpaRepository<Game, String> {
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select g
          from Game g
          join fetch g.matching m
          join fetch m.requester
          join fetch m.receiver
          join fetch g.sport
         where g.id = :gameId
        """
    )
    fun findByIdFetchAllForUpdate(
        gameId: String,
    ): Game?

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select g
          from Game g
          join fetch g.matching m
          join fetch m.requester
          join fetch m.receiver
         where g.id = :gameId
        """
    )
    fun findByIdFetchUsersForUpdate(gameId: String): Game?
}
