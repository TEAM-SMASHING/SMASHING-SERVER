package org.appjam.smashing.domain.review.dto.response

import org.appjam.smashing.domain.review.entity.GameReview
import org.appjam.smashing.domain.review.enums.ReviewRating

data class ReviewDetailResponse(
    val rating: ReviewRating,
    val reviewerNickname: String,
    val revieweeNickname: String,
    val tag: List<String>,
    val content: String?,
) {
    companion object {
        fun from(
            gr: GameReview,
        ) = ReviewDetailResponse(
            rating = gr.rating,
            reviewerNickname = gr.reviewer?.nickname ?: "알 수 없음",
            revieweeNickname = gr.reviewee?.nickname ?: "알 수 없음",
            tag = gr.tags.map { it.name },
            content = gr.content
        )
    }
}
