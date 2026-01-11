package org.appjam.smashing.domain.matching.dto.response

import org.appjam.smashing.domain.matching.dto.projection.SentMatchingSummaryProjection
import org.appjam.smashing.domain.matching.enums.MatchingStatus
import org.appjam.smashing.domain.user.enums.Gender
import java.time.OffsetDateTime

data class SentMatchingSummaryResponse(
    val matchingId: String,
    val createdAt: OffsetDateTime,
    val status: MatchingStatus,
    val receiver: ReceiverSummary,
) {

    data class ReceiverSummary(
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
            p: SentMatchingSummaryProjection
        ) = SentMatchingSummaryResponse(
            matchingId = p.matchingId,
            createdAt = p.createdAt,
            status = p.status,
            receiver = ReceiverSummary(
                userId = p.receiverUserId,
                nickname = p.receiverNickname,
                gender = p.receiverGender,
                reviewCount = p.receiverReviewCount,
                tierId = p.receiverTierId,
                tierName = p.receiverTierName,
                wins = p.receiverWins,
                losses = p.receiverLosses,
            )
        )

        fun from(list: List<SentMatchingSummaryProjection>) = list.map(::from)
    }
}
