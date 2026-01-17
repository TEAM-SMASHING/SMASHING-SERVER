package org.appjam.smashing.domain.game.dto.response

data class GameResultSubmitResponse(
    val reviewId: String? = null,
) {
    companion object {
        fun from(
            reviewId: String?
        ) = GameResultSubmitResponse(
            reviewId = reviewId
        )
    }
}
