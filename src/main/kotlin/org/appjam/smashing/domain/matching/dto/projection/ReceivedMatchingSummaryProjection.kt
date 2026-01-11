package org.appjam.smashing.domain.matching.dto.projection

import com.querydsl.core.annotations.QueryProjection
import org.appjam.smashing.domain.matching.enums.MatchingStatus
import org.appjam.smashing.domain.user.enums.Gender
import org.appjam.smashing.global.common.dto.CursorKey
import org.appjam.smashing.global.util.TimeUtils
import java.time.LocalDateTime
import java.time.OffsetDateTime

@QueryProjection
data class ReceivedMatchingSummaryProjection(
    val matchingId: String,
    val createdAtLdt: LocalDateTime,
    val status: MatchingStatus,
    val requesterUserId: String,
    val requesterNickname: String,
    val requesterGender: Gender,
    val requesterReviewCount: Long,
    val requesterTierId: Long,
    val requesterTierName: String,
    val requesterWins: Int,
    val requesterLosses: Int,
) : CursorKey {

    override val cursorId: String
        get() = matchingId

    val createdAt: OffsetDateTime
        get() = TimeUtils.toOffsetDateTime(createdAtLdt)
}
