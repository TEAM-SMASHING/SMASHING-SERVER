package org.appjam.smashing.domain.game.repository

import org.appjam.smashing.domain.game.entity.GameResultSubmission
import org.springframework.data.jpa.repository.JpaRepository

interface GameResultSubmissionRepository : JpaRepository<GameResultSubmission, String> {
    fun countByGame_Id(gameId: String): Long

    fun countByGame_IdAndSubmitter_Id(
        gameId: String,
        submitterUserId: String,
    ): Long
}
