package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.user.dto.projection.OtherUserRecommendationProjection

data class OtherUsersRecommendationResponse(
    val recommendedUsers: List<OtherUsers>,
) {
    data class OtherUsers(
        val userId: String,
        val nickname: String,
        val tierId: Long,
        val wins: Int,
        val losses: Int,
        val reviews: Int,
        val gender: String,
    ) {
        companion object {
            fun from(
                userId: String,
                nickname: String,
                tierId: Long,
                wins: Int,
                losses: Int,
                reviews: Int,
                gender: String,
            ) = OtherUsers(
                userId = userId,
                nickname = nickname,
                tierId = tierId,
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
                    tierId = profile.tierId,
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
