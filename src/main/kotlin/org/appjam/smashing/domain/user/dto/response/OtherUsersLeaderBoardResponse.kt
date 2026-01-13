package org.appjam.smashing.domain.user.dto.response

data class OtherUsersLeaderBoardResponse(
    val rank: Int,
    val userId: String,
    val nickname: String,
    val tierId: Long,
    val lp: Int,
)
