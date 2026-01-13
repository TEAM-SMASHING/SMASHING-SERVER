package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.user.entity.UserSportProfile

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
    ) {
        companion object {
            fun from(
                u: UserSportProfile,
                reviewCount: Int,
            ) = OtherUsers(
                userId = u.user.id!!,
                nickname = u.user.nickname,
                tierId = u.tier.id!!,
                wins = u.wins,
                losses = u.losses,
                reviews = reviewCount,
            )

            fun listForm(
                recommendedUsers: List<UserSportProfile>,
                reviewCounts: Map<String, Long>,
            ) = recommendedUsers.map { profile ->
                from(
                    u = profile,
                    reviewCount = reviewCounts[profile.user.id]?.toInt() ?: 0,
                )
            }
        }
    }

    companion object {
        fun from(
            recommendedUsers: List<UserSportProfile>,
            reviewCounts: Map<String, Long>,
        ) = OtherUsersRecommendationResponse(
            recommendedUsers = OtherUsers.listForm(
                recommendedUsers = recommendedUsers,
                reviewCounts = reviewCounts,
            )
        )
    }
}
