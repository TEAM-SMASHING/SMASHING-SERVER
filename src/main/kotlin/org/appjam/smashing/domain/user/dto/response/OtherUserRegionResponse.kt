package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.user.dto.projection.OtherUserRegionProjection

data class OtherUserRegionResponse(
    val userId: String,
    val nickname: String,
    val gender: String,
    val tierId: Long,
    val wins: Int,
    val losses: Int,
    val reviews: Long,
) {
    companion object {
        fun from(
            p: OtherUserRegionProjection,
        ) = OtherUserRegionResponse(
            userId = p.userId,
            nickname = p.nickname,
            gender = p.gender,
            tierId = p.tierId,
            wins = p.wins,
            losses = p.losses,
            reviews = p.reviews,
        )

        fun listForm(
            p: List<OtherUserRegionProjection>
        ) = p.map { from(it) }
    }
}
