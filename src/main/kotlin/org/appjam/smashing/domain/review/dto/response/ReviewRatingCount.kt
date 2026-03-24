package org.appjam.smashing.domain.review.dto.response

import com.querydsl.core.annotations.QueryProjection
import org.appjam.smashing.domain.review.enums.ReviewRating

@QueryProjection
data class ReviewRatingCount(
    val reviewRating: ReviewRating,
    val counts: Long,
)
