package org.appjam.smashing.domain.game.repository

import jakarta.persistence.LockModeType
import org.appjam.smashing.domain.game.entity.GameResultSubmission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface GameResultSubmissionRepository : JpaRepository<GameResultSubmission, String> {
    fun countByGame_Id(gameId: String): Long

    fun countByGame_IdAndSubmitter_Id(
        gameId: String,
        submitterUserId: String,
    ): Long

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
          join fetch s.submitter
          join fetch s.confirmer
          join fetch s.winner
          join fetch s.loser
         where s.id = :submissionId
           and s.game.id = :gameId
        """
    )
    fun findDetailByIdAndGameId(
        submissionId: String,
        gameId: String,
    ): GameResultSubmission?

    @Modifying
    @Query(
        """
        update GameResultSubmission s
           set s.deletedAt = CURRENT_TIMESTAMP
         where s.game.id = :gameId
        """
    )
    fun softDeleteAllByGameId(
        gameId: String
    ): Int
}
