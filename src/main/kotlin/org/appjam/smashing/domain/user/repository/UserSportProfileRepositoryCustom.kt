package org.appjam.smashing.domain.user.repository

import org.appjam.smashing.domain.user.dto.projection.OtherUserRecommendationProjection
import org.appjam.smashing.domain.user.dto.projection.OtherUserRegionProjection
import org.appjam.smashing.domain.user.dto.projection.OtherUserSearchProjection
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorPageResponse
import java.time.OffsetDateTime

interface UserSportProfileRepositoryCustom {
    fun findRandomRecommendation(
        region: String,
        sportId: Long,
        excludeUserId: String,
        myLp: Int,
        lpThreshold: Int,
        limit: Long,
    ): List<OtherUserRecommendationProjection>

    fun findAllBySportOrderByNickname(
        nickname: String,
        sportId: Long,
        excludeUserId: String,
    ): List<OtherUserSearchProjection>

    fun findAllBySportAndRegion(
        userId: String,
        sportId: Long,
        region: String,
        request: CommonCursorRequest,
        gender: String?,
        tierId: Long?,
        snapshotAt: OffsetDateTime,
    ): CursorPageResponse<OtherUserRegionProjection>
}
