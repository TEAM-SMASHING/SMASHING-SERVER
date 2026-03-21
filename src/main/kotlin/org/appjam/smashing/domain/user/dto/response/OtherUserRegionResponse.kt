package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.tier.enums.TierCode
import org.appjam.smashing.domain.user.dto.projection.OtherUserRegionProjection

data class OtherUserRegionResponse(
    val userProfileId: String,
    val nickname: String,
    val gender: String,
    val tierCode: TierCode,
    val wins: Int,
    val losses: Int,
    val reviews: Long,
) {
    companion object {
        fun from(
            p: OtherUserRegionProjection,
        ) = OtherUserRegionResponse(
            userProfileId = p.userProfileId,
            nickname = p.nickname,
            gender = p.gender,
            tierCode = p.tierCode,
            wins = p.wins,
            losses = p.losses,
            reviews = p.reviews,
        )

        fun listForm(
            p: List<OtherUserRegionProjection>
        ) = p.map { from(it) }
    }
}
