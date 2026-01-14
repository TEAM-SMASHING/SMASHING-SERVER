package org.appjam.smashing.domain.review.repository

import org.appjam.smashing.domain.review.dto.projection.UserRecentGameProjection
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorPageResponse
import java.time.OffsetDateTime

interface GameReviewRepositoryCustom {
    fun findAllBySportIdOrderByDate(
        request: CommonCursorRequest,
        activeSportId: Long,
        userId: String,
        snapshotAt: OffsetDateTime
    ): CursorPageResponse<UserRecentGameProjection>
}
