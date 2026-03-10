package org.appjam.smashing.domain.game.repository

import jakarta.persistence.LockModeType
import org.appjam.smashing.domain.game.entity.GameResultSubmission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface GameResultSubmissionRepository : JpaRepository<GameResultSubmission, String> {
    fun countByGame_Id(gameId: String): Long

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select s
        from GameResultSubmission s
        where s.id = :submissionId
          and s.game.id = :gameId
        """
    )
    fun findByIdAndGameIdForUpdate(
        submissionId: String,
        gameId: String,
    ): GameResultSubmission?

    @Query(
        """
        select s
          from GameResultSubmission s
          join fetch s.submitterProfile sp
          join fetch sp.user
          join fetch sp.tier
          join fetch s.confirmerProfile cp
          join fetch cp.user
          join fetch cp.tier
          join fetch s.winnerProfile wp
          join fetch wp.user
          join fetch s.loserProfile lp
          join fetch lp.user
         where s.id = :submissionId
           and s.game.id = :gameId
        """
    )
    fun findDetailByIdAndGameId(
        submissionId: String,
        gameId: String,
    ): GameResultSubmission?
}
