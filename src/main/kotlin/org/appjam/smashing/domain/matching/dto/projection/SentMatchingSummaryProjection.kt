package org.appjam.smashing.domain.matching.dto.projection

import com.querydsl.core.annotations.QueryProjection
import org.appjam.smashing.domain.matching.enums.MatchingStatus
import org.appjam.smashing.domain.tier.enums.TierCode
import org.appjam.smashing.domain.user.enums.Gender
import org.appjam.smashing.global.common.dto.CursorKey
import org.appjam.smashing.global.util.TimeUtils
import java.time.LocalDateTime
import java.time.OffsetDateTime

@QueryProjection
data class SentMatchingSummaryProjection(
    val matchingId: String,
    val createdAtLdt: LocalDateTime,
    val status: MatchingStatus,
    val receiverUserId: String,
    val receiverNickname: String,
    val receiverGender: Gender,
    val receiverReviewCount: Long,
    val receiverTierCode: TierCode,
    val receiverWins: Int,
    val receiverLosses: Int,
) : CursorKey {

    override val cursorId: String
        get() = matchingId

    val createdAt: OffsetDateTime
        get() = TimeUtils.toOffsetDateTime(createdAtLdt)
}
