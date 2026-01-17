package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.review.enums.ReviewRating
import org.appjam.smashing.domain.review.enums.ReviewTag

data class UserRecentReviewSummaryResponse(
    val ratingCounts: RatingCounts,
    val tagCounts: TagCounts
) {
    data class RatingCounts(
        val best: Long,
        val good: Long,
        val bad: Long,
    ) {
        companion object {
            fun from(map: Map<ReviewRating, Long>) = RatingCounts(
                best = map[ReviewRating.BEST] ?: 0,
                good = map[ReviewRating.GOOD] ?: 0,
                bad = map[ReviewRating.BAD] ?: 0
            )
        }
    }

    data class TagCounts(
        val goodManner: Long,
        val onTime: Long,
        val fairPlay: Long,
        val fastResponse: Long,
    ) {
        companion object {
            fun from(map: Map<ReviewTag, Long>) = TagCounts(
                goodManner = map[ReviewTag.GOOD_MANNER] ?: 0,
                onTime = map[ReviewTag.ON_TIME] ?: 0,
                fairPlay = map[ReviewTag.FAIR_PLAY] ?: 0,
                fastResponse = map[ReviewTag.FAST_RESPONSE] ?: 0
            )
        }
    }

    companion object {
        fun from(
            ratingMap: Map<ReviewRating, Long>,
            tagMap: Map<ReviewTag, Long>,
        ) = UserRecentReviewSummaryResponse(
            ratingCounts = RatingCounts.from(ratingMap),
            tagCounts = TagCounts.from(tagMap)
        )
    }
}
