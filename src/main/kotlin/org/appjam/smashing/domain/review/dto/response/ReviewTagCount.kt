package org.appjam.smashing.domain.review.dto.response

import com.querydsl.core.annotations.QueryProjection
import org.appjam.smashing.domain.review.enums.ReviewTag

data class ReviewTagCount @QueryProjection constructor(
    val reviewTag: ReviewTag,
    val counts: Long
)
