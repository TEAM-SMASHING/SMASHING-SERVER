package org.appjam.smashing.domain.user.dto.projection

import com.querydsl.core.annotations.QueryProjection
import org.appjam.smashing.domain.tier.enums.TierCode
import org.appjam.smashing.global.common.dto.CursorKey

@QueryProjection
data class OtherUserRegionProjection(
    val userId: String,
    val nickname: String,
    val gender: String,
    val tierCode: TierCode,
    val wins: Int,
    val losses: Int,
    val reviews: Long,
) : CursorKey {
    override val cursorId: String
        get() = userId
}
