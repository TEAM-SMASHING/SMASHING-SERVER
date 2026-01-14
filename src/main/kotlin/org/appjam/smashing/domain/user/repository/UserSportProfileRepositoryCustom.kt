package org.appjam.smashing.domain.user.repository

import org.appjam.smashing.domain.user.dto.projection.OtherUserRecommendationProjection
import org.appjam.smashing.domain.user.dto.projection.OtherUserSearchProjection

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
}
