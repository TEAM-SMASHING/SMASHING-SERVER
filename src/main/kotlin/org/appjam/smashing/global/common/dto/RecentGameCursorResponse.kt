package org.appjam.smashing.global.common.dto

import org.appjam.smashing.domain.review.dto.projection.UserRecentGameProjection
import org.appjam.smashing.domain.user.dto.response.UserRecentGameResponse
import java.time.OffsetDateTime

data class RecentGameCursorResponse(
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
        ) = RecentGameCursorResponse(
            snapshotAt = page.snapshotAt,
            ratingCounts = ratingCounts,
            tagCounts = tagCounts,
            games = UserRecentGameResponse.Game.listForm(page.results),
            nextCursor = page.nextCursor,
            hasNext = page.hasNext
        )
    }
}
