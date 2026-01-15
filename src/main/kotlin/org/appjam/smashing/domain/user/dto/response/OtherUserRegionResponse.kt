package org.appjam.smashing.domain.user.dto.response

data class OtherUserRegionResponse(
    val userId: String,
    val nickname: String,
    val gender: String,
    val tierId: Long,
    val wins: Int,
    val losses: Int,
    val reviews: Int,
)
