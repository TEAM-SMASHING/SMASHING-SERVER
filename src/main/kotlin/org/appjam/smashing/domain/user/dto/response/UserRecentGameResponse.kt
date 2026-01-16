package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.review.dto.projection.UserRecentGameProjection
import java.time.LocalDateTime

data class UserRecentGameMeta(
    val ratingCounts: RatingCounts,
    val tagCounts: TagCounts
) {
    data class RatingCounts(
        val best: Int,
        val good: Int,
        val bad: Int,
    ) {
        companion object {
            fun from(
                best: Int,
                good: Int,
                bad: Int
            ) =
                RatingCounts(
                    best = best,
                    good = good,
                    bad = bad
                )
        }
    }

    data class TagCounts(
        val goodManner: Int,
        val onTime: Int,
        val fairPlay: Int,
        val fastResponse: Int,
    ) {
        companion object {
            fun from(
                goodManner: Int,
                onTime: Int,
                fairPlay: Int,
                fastResponse: Int
            ) = TagCounts(
                goodManner = goodManner,
                onTime = onTime,
                fairPlay = fairPlay,
                fastResponse = fastResponse,
            )
        }
    }
}

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
