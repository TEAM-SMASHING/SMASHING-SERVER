package org.appjam.smashing.domain.matching.dto.response

import org.appjam.smashing.domain.matching.dto.projection.ReceivedMatchingSummaryProjection
import org.appjam.smashing.domain.user.enums.Gender
import org.appjam.smashing.domain.tier.enums.TierCode

data class ReceivedMatchingSummaryResponse(
    val matchingId: String,
    val requesterProfileId: String,
    val nickname: String,
    val gender: Gender,
    val tierCode: TierCode,
    val wins: Int,
    val losses: Int,
    val reviewCount: Long,
) {
    companion object {
        fun from(
            results: List<ReceivedMatchingSummaryProjection>
        ): List<ReceivedMatchingSummaryResponse> {
            return results.map {
                ReceivedMatchingSummaryResponse(
                    matchingId = it.matchingId,
                    requesterProfileId = it.requesterProfileId,
                    nickname = it.requesterNickname,
                    gender = it.requesterGender,
                    tierCode = it.requesterTierCode,
                    wins = it.requesterWins,
                    losses = it.requesterLosses,
                    reviewCount = it.requesterReviewCount,
                )
            }
        }
    }
}
