package org.appjam.smashing.domain.game.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.appjam.smashing.domain.game.dto.command.GameResultConfirmCommand
import org.appjam.smashing.domain.review.enums.ReviewRating
import org.appjam.smashing.domain.review.enums.ReviewTag
import org.appjam.smashing.global.common.validator.annotation.ValidEnum
import org.appjam.smashing.global.extensions.ofIgnoreCase
import org.appjam.smashing.global.extensions.ofIgnoreCaseOrNull

data class GameResultConfirmRequest(
    @field:NotNull(message = "review는 필수입니다.")
    @field:Valid
    val review: ReviewRequest?,
) {
    fun toCommand() = GameResultConfirmCommand(
        review = review!!.toCommand(),
    )

    data class ReviewRequest(
        @field:NotBlank(message = "review.rating은 필수입니다.")
        @field:ValidEnum(message = "잘못된 rating 값입니다.", enumClass = ReviewRating::class)
        val rating: String?,

        val content: String?,

        val tags: Set<@ValidEnum(message = "잘못된 tag 값입니다.", enumClass = ReviewTag::class) String>?,
    ) {
        fun toCommand() = GameResultConfirmCommand.ReviewCommand(
            rating = ofIgnoreCase<ReviewRating>(rating!!),
            content = content,
            tags = tags?.mapNotNull { ofIgnoreCaseOrNull<ReviewTag>(it) }?.toSet() ?: emptySet(),
        )
    }
}
