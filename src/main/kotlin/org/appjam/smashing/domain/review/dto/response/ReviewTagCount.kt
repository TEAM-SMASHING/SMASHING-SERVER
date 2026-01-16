package org.appjam.smashing.domain.review.dto.response

import org.appjam.smashing.domain.review.enums.ReviewTag

data class ReviewTagCount(
    val reviewTag: ReviewTag,
    val counts: Long
)
