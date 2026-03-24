package org.appjam.smashing.domain.review.dto.response

import com.querydsl.core.annotations.QueryProjection
import org.appjam.smashing.domain.review.enums.ReviewTag

@QueryProjection
data class ReviewTagCount(
    val reviewTag: ReviewTag,
    val counts: Long,
)
