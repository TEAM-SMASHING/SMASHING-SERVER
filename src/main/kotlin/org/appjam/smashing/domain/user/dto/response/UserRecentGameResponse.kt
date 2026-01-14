package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.review.dto.projection.UserRecentGameProjection
import org.appjam.smashing.global.common.dto.CursorPageResponse
import java.time.LocalDateTime
import java.time.OffsetDateTime

data class UserRecentGameResponse(
    val ratingCounts: RatingCounts,
    val tagCounts: TagCounts,
    val results: List<Game>,
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

    data class Game(
        val gameId: String,
        val reviewId: String,
        val opponentNickname: String,
        val confirmedAt: LocalDateTime,
        val content: String?,
    ) {
        companion object {
            fun from(
                p: UserRecentGameProjection
            ) = Game(
                gameId = p.gameId,
                reviewId = p.reviewId,
                opponentNickname = p.opponentNickname,
                confirmedAt = p.confirmedAt,
                content = p.content
            )

            fun listForm(
                projections: List<UserRecentGameProjection>
            ): List<Game> = projections.map { from(it) }
        }
    }

    companion object {
        fun from(
            ratingCounts: RatingCounts,
            tagCounts: TagCounts,
            projections: List<UserRecentGameProjection>
        ) = UserRecentGameResponse(
            ratingCounts = ratingCounts,
            tagCounts = tagCounts,
            results = Game.listForm(projections)
        )
    }
}

data class UserRecentGameCursorResponse(
    val snapshotAt: OffsetDateTime,
    val ratingCounts: UserRecentGameResponse.RatingCounts,
    val tagCounts: UserRecentGameResponse.TagCounts,
    val games: List<UserRecentGameResponse.Game>,
    val nextCursor: String?,
    val hasNext: Boolean
) {
    companion object {
        fun of(
            page: CursorPageResponse<UserRecentGameProjection>,
            ratingCounts: UserRecentGameResponse.RatingCounts,
            tagCounts: UserRecentGameResponse.TagCounts,
        ) = UserRecentGameCursorResponse(
            snapshotAt = page.snapshotAt,
            ratingCounts = ratingCounts,
            tagCounts = tagCounts,
            games = UserRecentGameResponse.Game.listForm(page.results),
            nextCursor = page.nextCursor,
            hasNext = page.hasNext
        )
    }
}
