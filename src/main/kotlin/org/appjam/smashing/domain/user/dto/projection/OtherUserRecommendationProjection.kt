package org.appjam.smashing.domain.user.dto.projection

import com.querydsl.core.annotations.QueryProjection

@QueryProjection
data class OtherUserRecommendationProjection(
    val userId: String,
    val nickname: String,
    val tierId: Long,
    val wins: Int,
    val losses: Int,
    val reviews: Int,
    val gender: String,
)
