package org.appjam.smashing.domain.user.dto.projection

import com.querydsl.core.annotations.QueryProjection

@QueryProjection
data class OtherUserSearchProjection(
    val userProfileId: String,
    val nickname: String,
)
