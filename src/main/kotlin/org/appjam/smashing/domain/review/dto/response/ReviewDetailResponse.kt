package org.appjam.smashing.domain.review.dto.response

data class ReviewDetailResponse(
    val reviewerNickname: String,
    val revieweeNickname: String,
    val tag: List<String>,
    val content: String,
)
