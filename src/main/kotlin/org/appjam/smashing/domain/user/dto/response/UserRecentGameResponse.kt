package org.appjam.smashing.domain.user.dto.response

import java.time.LocalDateTime

data class UserRecentGameResponse(
    val ratingCounts: RatingCounts,
    val tagCounts: TagCounts,
    val games: Game
) {
    data class RatingCounts(
        val best: Int,
        val good: Int,
        val bad: Int,
    )

    data class TagCounts(
        val goodManInt: Int,
        val onTime: Int,
        val fairPlay: Int,
        val fastResponse: Int,
    )

    data class Game(
        val gameId: String,
        val reviewId: String,
        val opponentNickname: String,
        val confirmedAt: LocalDateTime,
        val content: String?
    )
}
