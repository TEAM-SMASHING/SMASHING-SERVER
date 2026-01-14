package org.appjam.smashing.domain.user.repository

import org.appjam.smashing.domain.user.dto.projection.OtherUserRecommendationProjection

interface UserSportProfileRepositoryCustom {
    fun findRandomRecommendation(
        region: String,
        sportId: Long,
        excludeUserId: String,
        myLp: Int,
        lpThreshold: Int,
        limit: Long,
    ): List<OtherUserRecommendationProjection>
}
