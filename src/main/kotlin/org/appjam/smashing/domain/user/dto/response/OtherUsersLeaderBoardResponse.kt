package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.user.entity.UserSportProfile

data class OtherUsersLeaderBoardResponse(
    val topUsers: List<OtherUsers>,
    val user: UserInfo,
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

    data class UserInfo(
        val nickname: String,
        val tierId: Long,
        val lp: Int,
    ) {
        companion object {
            fun from(
                nickname: String,
                tierId: Long,
                lp: Int,
            ) = UserInfo(
                nickname = nickname,
                tierId = tierId,
                lp = lp,
            )
        }
    }

    companion object {
        fun from(
            topUsers: List<UserSportProfile>,
            nickname: String,
            tierId: Long,
            lp: Int,
        ) = OtherUsersLeaderBoardResponse(
            user = UserInfo.from(
                nickname = nickname,
                tierId = tierId,
                lp = lp,
            ),
            topUsers = OtherUsers.listForm(topUsers),
        )
    }
}
