package org.appjam.smashing.domain.user.dto.response

data class UserRecentReviewSummaryResponse(
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
                bad: Int,
            ) = RatingCounts(
                best = best,
                good = good,
                bad = bad,
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
                fastResponse: Int,
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
