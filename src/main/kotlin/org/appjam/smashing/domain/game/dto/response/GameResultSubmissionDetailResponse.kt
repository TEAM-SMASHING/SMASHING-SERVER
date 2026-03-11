package org.appjam.smashing.domain.game.dto.response

import org.appjam.smashing.domain.game.entity.GameResultSubmission

data class GameResultSubmissionDetailResponse(
    val attemptNo: Int,
    val submitter: SubmitterSummary,
    val winner: SideSummary,
    val loser: SideSummary,
) {

    data class SubmitterSummary(
        val userId: String,
        val nickname: String,
    )

    data class SideSummary(
        val userId: String,
        val nickname: String,
        val score: Int,
    )

//    companion object {
//        fun from(
//            submission: GameResultSubmission,
//            winnerScore: Int,
//            loserScore: Int,
//        ): GameResultSubmissionDetailResponse {
//            return GameResultSubmissionDetailResponse(
//                attemptNo = submission.attemptNo,
//                submitter = SubmitterSummary(
//                    userId = submission.submitter.id!!,
//                    nickname = submission.submitter.nickname,
//                ),
//                winner = SideSummary(
//                    userId = submission.winner.id!!,
//                    nickname = submission.winner.nickname,
//                    score = winnerScore,
//                ),
//                loser = SideSummary(
//                    userId = submission.loser.id!!,
//                    nickname = submission.loser.nickname,
//                    score = loserScore,
//                ),
//            )
//        }
//    }
}
