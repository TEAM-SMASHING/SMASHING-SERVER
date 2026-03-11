package org.appjam.smashing.domain.game.dto.response

data class GameResultConfirmResponse(
    val reviewId: String?,
) {
    companion object {
        fun from(
            reviewId: String?,
        ) = GameResultConfirmResponse(
            reviewId = reviewId,
        )
    }
}
