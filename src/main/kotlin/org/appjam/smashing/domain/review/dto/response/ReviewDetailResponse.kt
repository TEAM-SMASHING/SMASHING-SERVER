package org.appjam.smashing.domain.review.dto.response

import org.appjam.smashing.domain.review.entity.GameReview
import org.appjam.smashing.domain.review.enums.ReviewRating
import org.appjam.smashing.domain.user.entity.User.Companion.DELETED_USER_NICKNAME

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
            reviewerNickname = gr.reviewerProfile.user.nickname ?: DELETED_USER_NICKNAME, ///             reviewerNickname = gr.reviewer?.nickname ?: DELETED_USER_NICKNAME,

            revieweeNickname = gr.revieweeProfile.user.nickname,
            tag = gr.tags.map { it.name },
            content = gr.content
        )
    }
}
