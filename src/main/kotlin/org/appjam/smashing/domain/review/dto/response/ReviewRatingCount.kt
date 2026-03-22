package org.appjam.smashing.domain.review.dto.response

import com.querydsl.core.annotations.QueryProjection
import org.appjam.smashing.domain.review.enums.ReviewRating

data class ReviewRatingCount @QueryProjection constructor(
    val reviewRating: ReviewRating,
    val counts: Long,
)
