package org.appjam.smashing.domain.user.dto.projection

import com.querydsl.core.annotations.QueryProjection
import org.appjam.smashing.domain.tier.enums.TierCode

@QueryProjection
data class OtherUserRecommendationProjection(
    val userId: String,
    val nickname: String,
    val tierCode: TierCode,
    val wins: Int,
    val losses: Int,
    val reviews: Int,
    val gender: String,
)
