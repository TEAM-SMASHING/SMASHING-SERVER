package org.appjam.smashing.domain.game.repository

import org.appjam.smashing.domain.game.entity.Game
import org.springframework.data.jpa.repository.JpaRepository

interface GameRepository : JpaRepository<Game, String> {
    fun existsByMatchingId(
        matchingId: String
    ): Boolean

    fun findByMatchingId(
        matchingId: String
    ): Game?
}
