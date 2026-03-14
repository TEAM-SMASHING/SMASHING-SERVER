package org.appjam.smashing.domain.game.repository

import org.appjam.smashing.domain.game.dto.projection.PendingResultAcceptedGameProjection
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorPageResponse
import java.time.OffsetDateTime

interface GameRepositoryCustom {

//    fun fetchPendingResultAcceptedGamesPage(
//        userId: String,
//        sportId: Long,
//        request: CommonCursorRequest,
//        snapshotAt: OffsetDateTime,
//    ): CursorPageResponse<PendingResultAcceptedGameProjection>
}
