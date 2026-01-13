package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.user.entity.UserSportProfile

data class OtherUsersRecommendationResponse(
    val recommendedUsers: List<OtherUsers>,
) {
    data class OtherUsers(
        val userId: String,
        val nickname: String,
        val tierId: Long,
    ) {
        companion object {
            fun from(
                userId: String,
                nickname: String,
                tierId: Long,
            ) = OtherUsers(
                userId = userId,
                nickname = nickname,
                tierId = tierId,
            )

            fun listForm(
                recommendedUsers: List<UserSportProfile>
            ) = recommendedUsers.map { userSportProfile ->
                from(
                    userId = userSportProfile.user.id!!,
                    nickname = userSportProfile.user.nickname,
                    tierId = userSportProfile.tier.id!!,
                )
            }
        }
    }

    companion object {
        fun from(
            recommendedUsers: List<UserSportProfile>,
        ) = OtherUsersRecommendationResponse(
            recommendedUsers = OtherUsers.listForm(
                recommendedUsers = recommendedUsers
            )
        )
    }
}
