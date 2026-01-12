package org.appjam.smashing.domain.user.dto.response

data class OtherUsersRecommendationResponse(
    val users: List<OtherUsers>,
) {
    data class OtherUsers(
        val userId: String,
        val nickname: String,
        val tierId: Int,
    ) {
        companion object {
            fun from(
                userId: String,
                nickname: String,
                tierId: Int,
            ) = OtherUsers(
                userId = userId,
                nickname = nickname,
                tierId = tierId,
            )
        }
    }

    companion object {
        fun from(users: List<OtherUsers>) = OtherUsersRecommendationResponse(
            users = users
        )
    }
}
