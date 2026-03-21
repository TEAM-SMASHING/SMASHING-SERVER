package org.appjam.smashing.domain.game.dto.response

import org.appjam.smashing.domain.game.dto.projection.PendingResultAcceptedGameProjection
import org.appjam.smashing.domain.game.enums.GameStatus
import org.appjam.smashing.domain.tier.enums.TierCode
import org.appjam.smashing.domain.user.enums.Gender
import java.time.OffsetDateTime

data class PendingResultAcceptedGameSummaryResponse(
    val gameId: String,
    val createdAt: OffsetDateTime,
    val resultStatus: GameStatus,
    val opponent: OpponentSummary,
    val latestSubmissionId: String?,
    val latestAttemptNo: Int?,
    val latestSubmitterProfileId: String?,
) {
    data class OpponentSummary(
        val opponentProfileId: String,
        val nickname: String,
        val gender: Gender,
        val tierCode: TierCode,
    )

    companion object {
        fun from(
            projection: PendingResultAcceptedGameProjection,
        ) = PendingResultAcceptedGameSummaryResponse(
            gameId = projection.gameId,
            createdAt = projection.createdAt,
            resultStatus = projection.resultStatus,
            opponent = OpponentSummary(
                opponentProfileId = projection.opponentProfileId,
                nickname = projection.opponentNickname,
                gender = projection.opponentGender,
                tierCode = projection.opponentTierCode,
            ),
            latestSubmissionId = projection.latestSubmissionId,
            latestAttemptNo = projection.latestAttemptNo,
            latestSubmitterProfileId = projection.latestSubmitterProfileId,
        )
    }
}
