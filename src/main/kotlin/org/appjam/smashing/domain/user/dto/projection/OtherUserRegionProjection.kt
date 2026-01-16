package org.appjam.smashing.domain.user.dto.projection

import com.querydsl.core.annotations.QueryProjection
import org.appjam.smashing.global.common.dto.CursorKey

@QueryProjection
data class OtherUserRegionProjection(
    val userId: String,
    val nickname: String,
    val gender: String,
    val tierId: Long,
    val wins: Int,
    val losses: Int,
    val reviews: Int,
) : CursorKey {
    override val cursorId: String
        get() = userId
}
