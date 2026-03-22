package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.tier.enums.TierCode
import org.appjam.smashing.domain.user.entity.UserSportProfile

data class OtherUsersLeaderBoardResponse(
    val topUsers: List<OtherUsers>,
    val user: UserInfo,
) {
    data class OtherUsers(
        val rank: Int,
        val userProfileId: String,
        val nickname: String,
        val tierCode: TierCode,
        val lp: Int,
    ) {
        companion object {
            fun from(
                u: UserSportProfile,
                rank: Int,
            ) = OtherUsers(
                rank = rank,
                userProfileId = u.id!!,
                nickname = u.user.nickname,
                tierCode = u.tier.code,
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
        val tierCode: TierCode,
        val lp: Int,
    ) {
        companion object {
            fun from(
                nickname: String,
                tierCode: TierCode,
                lp: Int,
            ) = UserInfo(
                nickname = nickname,
                tierCode = tierCode,
                lp = lp,
            )
        }
    }

    companion object {
        fun from(
            topUsers: List<UserSportProfile>,
            nickname: String,
            tierCode: TierCode,
            lp: Int,
        ) = OtherUsersLeaderBoardResponse(
            user = UserInfo.from(
                nickname = nickname,
                tierCode = tierCode,
                lp = lp,
            ),
            topUsers = OtherUsers.listForm(topUsers),
        )
    }
}
