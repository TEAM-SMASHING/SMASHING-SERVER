package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.user.entity.UserSportProfile

data class OtherUserProfilesResponse(
    val nickname: String,
    val selectedSport: SelectedSport,
    val sports: List<SportInfo>,
) {
    data class SelectedSport(
        val profileId: String,
        val sportCode: String,
        val tier: Int,
        val lp: Int,
        val minLp: Int,
        val maxLp: Int,
        val wins: Int,
        val losses: Int,
    ) {
        companion object {
            fun from(
                u: UserSportProfile,
            ) = SelectedSport(
                profileId = u.id!!,
                sportCode = u.sport.code,
                tier = u.tier.orderNo,
                lp = u.lp,
                minLp = u.tier.minLp,
                maxLp = u.tier.maxLp,
                wins = u.wins,
                losses = u.losses,
            )
        }
    }

    data class SportInfo(
        val profileId: String,
        val sportCode: String,
    ) {
        companion object {
            fun from(
                u: UserSportProfile,
            ) = SportInfo(
                profileId = u.id!!,
                sportCode = u.sport.code,
            )

            fun listForm(
                allProfiles: List<UserSportProfile>,
                selectedSportProfileId: String,
            ): List<SportInfo> = allProfiles
                .filter { userSportProfile ->
                    userSportProfile.id != selectedSportProfileId
                }
                .map { userSportProfile ->
                    from(userSportProfile)
                }
        }
    }

    companion object {
        fun from(
            nickname: String,
            selectedSport: SelectedSport,
            allProfiles: List<UserSportProfile>,
        ) = OtherUserProfilesResponse(
            nickname = nickname,
            selectedSport = selectedSport,
            sports = SportInfo.listForm(
                allProfiles,
                selectedSport.profileId
            )
        )
    }
}
