package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.tier.enums.TierCode
import org.appjam.smashing.domain.user.dto.projection.OtherUserRecommendationProjection

data class OtherUsersRecommendationResponse(
    val recommendedUsers: List<OtherUsers>,
) {
    data class OtherUsers(
        val userId: String,
        val nickname: String,
        val tierCode: TierCode,
        val wins: Int,
        val losses: Int,
        val reviews: Int,
        val gender: String,
    ) {
        companion object {
            fun from(
                userId: String,
                nickname: String,
                tierCode: TierCode,
                wins: Int,
                losses: Int,
                reviews: Int,
                gender: String,
            ) = OtherUsers(
                userId = userId,
                nickname = nickname,
                tierCode = tierCode,
                wins = wins,
                losses = losses,
                reviews = reviews,
                gender = gender,
            )

            fun listForm(
                recommendedUsers: List<OtherUserRecommendationProjection>,
            ) = recommendedUsers.map { profile ->
                from(
                    userId = profile.userId,
                    nickname = profile.nickname,
                    tierCode = profile.tierCode,
                    wins = profile.wins,
                    losses = profile.losses,
                    reviews = profile.reviews,
                    gender = profile.gender,
                )
            }
        }
    }

    companion object {
        fun from(
            recommendedUsers: List<OtherUserRecommendationProjection>,
        ) = OtherUsersRecommendationResponse(
            recommendedUsers = OtherUsers.listForm(recommendedUsers)
        )
    }
}
