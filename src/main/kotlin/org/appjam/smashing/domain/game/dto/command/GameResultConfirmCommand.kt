package org.appjam.smashing.domain.game.dto.command

import org.appjam.smashing.domain.review.enums.ReviewRating
import org.appjam.smashing.domain.review.enums.ReviewTag

data class GameResultConfirmCommand(
    val review: ReviewCommand,
) {
    data class ReviewCommand(
        val rating: ReviewRating,
        val content: String?,
        val tags: Set<ReviewTag>,
    )
}
