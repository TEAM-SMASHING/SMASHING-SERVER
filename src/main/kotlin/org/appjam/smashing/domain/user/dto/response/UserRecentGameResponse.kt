package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.review.dto.projection.UserRecentGameProjection
import java.time.LocalDateTime

data class UserRecentGameResponse(
    val gameReviewId: String,
    val opponentNickname: String,
    val createdAt: LocalDateTime,
    val content: String?,
) {
    companion object {
        fun from(
            p: UserRecentGameProjection
        ) = UserRecentGameResponse(
            gameReviewId = p.gameReviewId,
            opponentNickname = p.opponentNickname,
            createdAt = p.createdAt,
            content = p.content
        )

        fun listForm(
            projections: List<UserRecentGameProjection>
        ): List<UserRecentGameResponse> = projections.map { from(it) }
    }
}

