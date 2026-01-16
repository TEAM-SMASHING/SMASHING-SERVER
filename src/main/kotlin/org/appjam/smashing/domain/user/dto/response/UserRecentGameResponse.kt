package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.review.dto.projection.UserRecentGameProjection
import java.time.LocalDateTime

data class UserRecentGameResult(
    val gameId: String,
    val reviewId: String,
    val opponentNickname: String,
    val confirmedAt: LocalDateTime,
    val content: String?,
) {
    companion object {
        fun from(
            p: UserRecentGameProjection
        ) = UserRecentGameResult(
            gameId = p.gameId,
            reviewId = p.reviewId,
            opponentNickname = p.opponentNickname,
            confirmedAt = p.confirmedAt,
            content = p.content
        )

        fun listForm(
            projections: List<UserRecentGameProjection>
        ): List<UserRecentGameResult> = projections.map { from(it) }
    }
}
