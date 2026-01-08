package org.appjam.smashing.domain.game.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.appjam.smashing.domain.game.dto.command.GameResultSubmitCommand
import org.appjam.smashing.domain.review.enums.ReviewRating
import org.appjam.smashing.domain.review.enums.ReviewTag
import org.appjam.smashing.global.common.validator.annotation.ValidEnum
import org.appjam.smashing.global.extensions.ofIgnoreCase
import org.appjam.smashing.global.extensions.ofIgnoreCaseOrNull

data class GameResultSubmitRequest(
    @field:NotBlank(message = "winnerUserId는 필수입니다.")
    val winnerUserId: String?,

    @field:NotBlank(message = "loserUserId는 필수입니다.")
    val loserUserId: String?,

    @field:NotNull(message = "scoreWinner는 필수입니다.")
    @field:Min(value = 0, message = "scoreWinner는 0 이상이어야 합니다.")
    val scoreWinner: Int?,

    @field:NotNull(message = "scoreLoser는 필수입니다.")
    @field:Min(value = 0, message = "scoreLoser는 0 이상이어야 합니다.")
    val scoreLoser: Int?,

    @field:Valid
    val review: ReviewRequest? = null,
) {
    fun toCommand() = GameResultSubmitCommand(
        winnerUserId = winnerUserId!!,
        loserUserId = loserUserId!!,
        scoreWinner = scoreWinner!!,
        scoreLoser = scoreLoser!!,
        review = review?.toCommand(),
    )

    data class ReviewRequest(
        @field:NotBlank(message = "review.rating은 필수입니다.")
        @field:ValidEnum(message = "잘못된 rating 값입니다. (BAD/GOOD/BEST)", enumClass = ReviewRating::class)
        val rating: String?,

        val content: String?,

        val tags: Set<@ValidEnum(message = "잘못된 tag 값입니다. (MANNER_GOOD/ON_TIME/SPORTSMANSHIP)", enumClass = ReviewTag::class) String>?,
    ) {
        fun toCommand() = GameResultSubmitCommand.ReviewCommand(
            rating = ofIgnoreCase<ReviewRating>(rating!!),
            content = content,
            tags = tags?.mapNotNull { ofIgnoreCaseOrNull<ReviewTag>(it) }?.toSet(),
        )
    }
}
