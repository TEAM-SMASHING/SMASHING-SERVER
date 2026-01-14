package org.appjam.smashing.domain.review.dto.projection

import com.querydsl.core.annotations.QueryProjection
import org.appjam.smashing.global.common.dto.CursorKey
import java.time.LocalDateTime

@QueryProjection
data class UserRecentGameProjection(
    val gameId: String,
    val reviewId: String,
    val opponentNickname: String,
    val confirmedAt: LocalDateTime,
    val content: String?,
    override val cursorId: String = gameId
) : CursorKey
