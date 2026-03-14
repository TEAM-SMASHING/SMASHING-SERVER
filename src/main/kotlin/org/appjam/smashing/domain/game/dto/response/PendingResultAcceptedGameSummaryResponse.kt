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
    val submitAvailableAt: OffsetDateTime,
    val remainingSeconds: Long,
    val isSubmitLocked: Boolean,
    val latestSubmissionId: String? = null,
    val latestAttemptNo: Int?,
    val latestSubmitterId: String? = null,
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
            latestSubmitterId = projection.latestSubmitterId
        )
    }
}
