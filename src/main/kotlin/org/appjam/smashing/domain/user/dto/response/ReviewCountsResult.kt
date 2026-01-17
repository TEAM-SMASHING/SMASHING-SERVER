package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.review.enums.ReviewRating
import org.appjam.smashing.domain.review.enums.ReviewTag

data class ReviewCountsResult(
    val ratingMap: Map<ReviewRating, Long>,
    val tagMap: Map<ReviewTag, Long>
)