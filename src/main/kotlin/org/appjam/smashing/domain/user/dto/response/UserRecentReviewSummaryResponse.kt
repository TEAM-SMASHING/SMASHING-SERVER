package org.appjam.smashing.domain.user.dto.response

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
            fun from(
                best: Long,
                good: Long,
                bad: Long,
            ) = RatingCounts(
                best = best,
                good = good,
                bad = bad,
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
            fun from(
                goodManner: Long,
                onTime: Long,
                fairPlay: Long,
                fastResponse: Long,
            ) = TagCounts(
                goodManner = goodManner,
                onTime = onTime,
                fairPlay = fairPlay,
                fastResponse = fastResponse,
            )
        }
    }

    companion object {
        fun from(
            ratingCounts: RatingCounts,
            tagCounts: TagCounts,
        ) = UserRecentReviewSummaryResponse(
            ratingCounts = RatingCounts.from(
                best = ratingCounts.best,
                good = ratingCounts.good,
                bad = ratingCounts.bad,
            ),
            tagCounts = TagCounts.from(
                goodManner = tagCounts.goodManner,
                onTime = tagCounts.onTime,
                fairPlay = tagCounts.fairPlay,
                fastResponse = tagCounts.fastResponse,
            ),
        )
    }
}
