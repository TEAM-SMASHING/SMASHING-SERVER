package org.appjam.smashing.domain.review.dto.response

import org.appjam.smashing.domain.review.entity.GameReview

data class ReviewDetailResponse(
    val reviewerNickname: String,
    val revieweeNickname: String,
    val tag: List<String>,
    val content: String,
) {
    companion object {
        fun from(
            r: GameReview,
        ) = ReviewDetailResponse(
            reviewerNickname = r.reviewer.nickname,
            revieweeNickname = r.reviewee.nickname,
            tag = r.tags.map { it.name },
            content = r.content.orEmpty()
        )
    }
}
