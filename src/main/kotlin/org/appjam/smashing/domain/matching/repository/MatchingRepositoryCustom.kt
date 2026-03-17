package org.appjam.smashing.domain.matching.repository

import org.appjam.smashing.domain.matching.dto.projection.ReceivedMatchingSummaryProjection
import org.appjam.smashing.domain.matching.dto.projection.SentMatchingSummaryProjection
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorPageResponse
import java.time.OffsetDateTime

interface MatchingRepositoryCustom {

    fun fetchReceivedRequestedPage(
        receiverUserId: String,
        sportId: Long,
        request: CommonCursorRequest,
        snapshotAt: OffsetDateTime,
    ): CursorPageResponse<ReceivedMatchingSummaryProjection>

//    fun fetchSentRequestedPage(
//        requesterUserId: String,
//        sportId: Long,
//        request: CommonCursorRequest,
//        snapshotAt: OffsetDateTime,
//    ): CursorPageResponse<SentMatchingSummaryProjection>
}
