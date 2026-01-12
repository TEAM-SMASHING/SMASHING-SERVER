package org.appjam.smashing.domain.matching.dto.response

import org.appjam.smashing.domain.matching.enums.MatchingStatus
import org.appjam.smashing.domain.matching.dto.projection.ReceivedMatchingSummaryProjection
import org.appjam.smashing.domain.user.enums.Gender
import java.time.OffsetDateTime

data class ReceivedMatchingSummaryResponse(
    val matchingId: String,
    val createdAt: OffsetDateTime,
    val status: MatchingStatus,
    val requester: RequesterSummary,
) {

    data class RequesterSummary(
        val userId: String,
        val nickname: String,
        val gender: Gender,
        val reviewCount: Long,
        val tierId: Long,
        val tierName: String,
        val wins: Int,
        val losses: Int,
    )

    companion object {
        fun from(
            p: ReceivedMatchingSummaryProjection
        ) = ReceivedMatchingSummaryResponse(
            matchingId = p.matchingId,
            createdAt = p.createdAt,
            status = p.status,
            requester = RequesterSummary(
                userId = p.requesterUserId,
                nickname = p.requesterNickname,
                gender = p.requesterGender,
                reviewCount = p.requesterReviewCount,
                tierId = p.requesterTierId,
                tierName = p.requesterTierName,
                wins = p.requesterWins,
                losses = p.requesterLosses,
            )
        )

        fun from(list: List<ReceivedMatchingSummaryProjection>) = list.map(::from)
    }
}
