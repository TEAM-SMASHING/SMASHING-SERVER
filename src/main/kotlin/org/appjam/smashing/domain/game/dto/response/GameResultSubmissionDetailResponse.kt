package org.appjam.smashing.domain.game.dto.response

import org.appjam.smashing.domain.game.entity.GameResultSubmission

data class GameResultSubmissionDetailResponse(
    val attemptNo: Int,
    val submitter: SubmitterSummary,
    val winner: PlayerSummary,
    val loser: PlayerSummary,
) {
    data class SubmitterSummary(
        val userId: String,
        val nickname: String,
        val profileId: String,
    )

    data class PlayerSummary(
        val profileId: String,
        val nickname: String,
    )

    companion object {
        fun from(
            submission: GameResultSubmission,
        )= GameResultSubmissionDetailResponse(
                attemptNo = submission.attemptNo,
                submitter = SubmitterSummary(
                    userId = submission.submitterProfile.user.id!!,
                    nickname = submission.submitterProfile.user.nickname,
                    profileId = submission.submitterProfile.id!!,
                ),
                winner = PlayerSummary(
                    profileId = submission.winnerProfile.id!!,
                    nickname = submission.winnerProfile.user.nickname,
                ),
                loser = PlayerSummary(
                    profileId = submission.loserProfile.id!!,
                    nickname = submission.loserProfile.user.nickname,
                ),
            )
        }
}
