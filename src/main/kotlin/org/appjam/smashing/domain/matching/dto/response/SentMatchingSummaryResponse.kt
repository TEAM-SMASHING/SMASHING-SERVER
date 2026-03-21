package org.appjam.smashing.domain.matching.dto.response

import org.appjam.smashing.domain.matching.dto.projection.SentMatchingSummaryProjection
import org.appjam.smashing.domain.matching.enums.MatchingStatus
import org.appjam.smashing.domain.tier.enums.TierCode
import org.appjam.smashing.domain.user.enums.Gender
import java.time.OffsetDateTime

data class SentMatchingSummaryResponse(
    val matchingId: String,
    val createdAt: OffsetDateTime,
    val status: MatchingStatus,
    val receiver: ReceiverSummary,
) {
    data class ReceiverSummary(
        val receiverProfileId: String,
        val nickname: String,
        val gender: Gender,
        val reviewCount: Long,
        val tierCode: TierCode,
        val wins: Int,
        val losses: Int,
    )

    companion object {
        fun from(
            results: List<SentMatchingSummaryProjection>
        ): List<SentMatchingSummaryResponse> {
            return results.map {
                SentMatchingSummaryResponse(
                    matchingId = it.matchingId,
                    createdAt = it.createdAt,
                    status = it.status,
                    receiver = ReceiverSummary(
                        receiverProfileId = it.receiverProfileId,
                        nickname = it.receiverNickname,
                        gender = it.receiverGender,
                        reviewCount = it.receiverReviewCount,
                        tierCode = it.receiverTierCode,
                        wins = it.receiverWins,
                        losses = it.receiverLosses,
                    )
                )
            }
        }
    }
}
