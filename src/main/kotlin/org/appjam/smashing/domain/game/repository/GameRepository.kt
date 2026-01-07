package org.appjam.smashing.domain.game.repository

import org.appjam.smashing.domain.game.entity.Game
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface GameRepository : JpaRepository<Game, String> {
    fun existsByMatchingId(
        matchingId: String
    ): Boolean

    fun findByMatchingId(
        matchingId: String
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
}
