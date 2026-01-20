package org.appjam.smashing.domain.game.dto.response

import org.appjam.smashing.domain.game.dto.projection.PendingResultAcceptedGameProjection
import org.appjam.smashing.domain.game.enums.GameResultStatus
import org.appjam.smashing.domain.tier.enums.TierCode
import org.appjam.smashing.domain.user.enums.Gender
import java.time.OffsetDateTime

data class PendingResultAcceptedGameSummaryResponse(
    val gameId: String,
    val createdAt: OffsetDateTime,
    val resultStatus: GameResultStatus,
    val opponent: OpponentSummary,
    val submitAvailableAt: OffsetDateTime,
    val remainingSeconds: Long,
    val isSubmitLocked: Boolean,
    val latestSubmissionId: String? = null,
    val latestAttemptNo: Int?,
) {

    data class OpponentSummary(
        val userId: String,
        val nickname: String,
        val openchatUrl: String?,
        val gender: Gender,
        val tierCode: TierCode
    )

    companion object {
        fun from(
            projection: PendingResultAcceptedGameProjection,
            submitAvailableAt: OffsetDateTime,
            remainingSeconds: Long,
            isSubmitLocked: Boolean,
        ) = PendingResultAcceptedGameSummaryResponse(
            gameId = projection.gameId,
            createdAt = projection.createdAt,
            resultStatus = projection.resultStatus,
            opponent = OpponentSummary(
                userId = projection.opponentUserId,
                nickname = projection.opponentNickname,
                openchatUrl = projection.opponentOpenchatUrl,
                gender = projection.opponentGender,
                tierCode = projection.opponentTierCode
            ),
            submitAvailableAt = submitAvailableAt,
            remainingSeconds = remainingSeconds,
            isSubmitLocked = isSubmitLocked,
            latestSubmissionId = projection.latestSubmissionId,
            latestAttemptNo = projection.latestAttemptNo,
        )
    }
}
