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
            gr: GameReview,
        ) = ReviewDetailResponse(
            reviewerNickname = gr.reviewer.nickname,
            revieweeNickname = gr.reviewee.nickname,
            tag = gr.tags.map { it.name },
            content = gr.content.orEmpty()
        )
    }
}
