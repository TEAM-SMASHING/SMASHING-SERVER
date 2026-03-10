package org.appjam.smashing.domain.game.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.appjam.smashing.domain.game.dto.command.GameResultSubmitCommand
import org.appjam.smashing.domain.review.enums.ReviewRating
import org.appjam.smashing.domain.review.enums.ReviewTag
import org.appjam.smashing.global.common.validator.annotation.ValidEnum
import org.appjam.smashing.global.extensions.ofIgnoreCase
import org.appjam.smashing.global.extensions.ofIgnoreCaseOrNull

data class GameResultSubmitRequest(
    @field:NotBlank(message = "winnerProfileId는 필수입니다.")
    val winnerProfileId: String?,

    @field:NotBlank(message = "loserProfileId는 필수입니다.")
    val loserProfileId: String?,

    @field:NotNull(message = "review는 필수입니다.")
    @field:Valid
    val review: ReviewRequest?,
) {
    fun toCommand() = GameResultSubmitCommand(
        winnerProfileId = winnerProfileId!!,
        loserProfileId = loserProfileId!!,
        review = review!!.toCommand(),
    )

    data class ReviewRequest(
        @field:NotBlank(message = "review.rating은 필수입니다.")
        @field:ValidEnum(message = "잘못된 rating 값입니다.", enumClass = ReviewRating::class)
        val rating: String?,

        @field:Size(max = 100, message = "review.content는 100자 이하여야 합니다.")
        val content: String?,

        val tags: Set<@ValidEnum(message = "잘못된 tag 값입니다.", enumClass = ReviewTag::class) String>? = emptySet(),
    ) {
        fun toCommand(): GameResultSubmitCommand.ReviewCommand {
            val normalizedContent = content?.trim()?.takeIf { it.isNotEmpty() }

            return GameResultSubmitCommand.ReviewCommand(
                rating = ofIgnoreCase<ReviewRating>(rating!!),
                content = normalizedContent,
                tags = tags?.mapNotNull { ofIgnoreCaseOrNull<ReviewTag>(it) }?.toSet() ?: emptySet(),
            )
        }
    }
}
