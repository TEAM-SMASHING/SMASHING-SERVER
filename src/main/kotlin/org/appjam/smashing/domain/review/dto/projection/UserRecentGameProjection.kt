package org.appjam.smashing.domain.review.dto.projection

import com.querydsl.core.annotations.QueryProjection
import org.appjam.smashing.global.common.dto.CursorKey
import java.time.LocalDateTime

@QueryProjection
data class UserRecentGameProjection(
    val best: Int,
    val good: Int,
    val bad: Int,
    val goodManner: Int,
    val onTime: Int,
    val fairPlay: Int,
    val fastResponse: Int,
    val gameId: String,
    val reviewId: String,
    val opponentNickname: String,
    val confirmedAt: LocalDateTime,
    val content: String?,
) : CursorKey {
    override val cursorId: String = gameId
}
