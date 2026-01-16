package org.appjam.smashing.domain.review.dto.response

import org.appjam.smashing.domain.review.enums.ReviewRating

data class ReviewRatingCount(
    val reviewRating: ReviewRating,
    val counts: Long
)
