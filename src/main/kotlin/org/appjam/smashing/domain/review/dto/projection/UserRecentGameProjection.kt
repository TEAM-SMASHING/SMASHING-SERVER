package org.appjam.smashing.domain.review.dto.projection

import com.querydsl.core.annotations.QueryProjection
import org.appjam.smashing.global.common.dto.CursorKey
import java.time.LocalDateTime

@QueryProjection
data class UserRecentGameProjection(
    val gameReviewId: String,
    val reviewId: String,
    val opponentNickname: String,
    val createdAt: LocalDateTime,
    val content: String?,
) : CursorKey {
    override val cursorId: String
        get() = gameReviewId
}
