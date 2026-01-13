package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.user.entity.UserSportProfile

data class OtherUsersLeaderBoardResponse(
    val topUsers: List<OtherUsers>,
) {
    data class OtherUsers(
        val rank: Int,
        val userId: String,
        val nickname: String,
        val tierId: Long,
        val lp: Int,
    ) {
        companion object {
            fun from(
                u: UserSportProfile,
                rank: Int,
            ) = OtherUsers(
                rank = rank,
                userId = u.user.id!!,
                nickname = u.user.nickname,
                tierId = u.tier.id!!,
                lp = u.lp,
            )

            fun listForm(
                profiles: List<UserSportProfile>
            ) = profiles.mapIndexed { index, profile ->
                from(
                    u = profile,
                    rank = index + 1
                )
            }
        }
    }

    companion object {
        fun from(
            topUsers: List<UserSportProfile>
        ) = OtherUsersLeaderBoardResponse(
            topUsers = OtherUsers.listForm(topUsers),
        )
    }
}
