package org.appjam.smashing.domain.user.dto.response

data class CountsResult(
    val ratingCounts: UserRecentReviewSummaryResponse.RatingCounts,
    val tagCounts: UserRecentReviewSummaryResponse.TagCounts,
)
