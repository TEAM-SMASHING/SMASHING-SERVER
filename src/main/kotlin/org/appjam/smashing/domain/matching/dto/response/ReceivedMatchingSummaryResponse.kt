package org.appjam.smashing.domain.matching.dto.response

import org.appjam.smashing.domain.matching.dto.projection.ReceivedMatchingSummaryProjection
import org.appjam.smashing.domain.matching.enums.MatchingStatus
import org.appjam.smashing.domain.tier.enums.TierCode
import org.appjam.smashing.domain.user.enums.Gender
import java.time.OffsetDateTime

data class ReceivedMatchingSummaryResponse(
    val matchingId: String,
    val createdAt: OffsetDateTime,
    val status: MatchingStatus,
    val requester: RequesterSummary,
) {
    data class RequesterSummary(
        val requesterProfileId: String,
        val nickname: String,
        val gender: Gender,
        val reviewCount: Long,
        val tierCode: TierCode,
        val wins: Int,
        val losses: Int,
    )

    companion object {
        fun from(
            results: List<ReceivedMatchingSummaryProjection>
        ): List<ReceivedMatchingSummaryResponse> {
            return results.map {
                ReceivedMatchingSummaryResponse(
                    matchingId = it.matchingId,
                    createdAt = it.createdAt,
                    status = it.status,
                    requester = RequesterSummary(
                        requesterProfileId = it.requesterProfileId,
                        nickname = it.requesterNickname,
                        gender = it.requesterGender,
                        reviewCount = it.requesterReviewCount,
                        tierCode = it.requesterTierCode,
                        wins = it.requesterWins,
                        losses = it.requesterLosses,
                    )
                )
            }
        }
    }
}
