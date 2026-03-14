package org.appjam.smashing.domain.game.dto.response

data class GameResultSubmitResponse(
    val submissionId: String,
) {
    companion object {
        fun from(
            submissionId: String,
        ) = GameResultSubmitResponse(
            submissionId = submissionId,
        )
    }
}
