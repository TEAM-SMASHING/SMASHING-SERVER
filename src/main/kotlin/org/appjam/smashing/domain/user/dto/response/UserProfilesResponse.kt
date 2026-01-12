package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.user.entity.UserSportProfile

data class UserProfilesResponse(
    val nickname: String,
    val activeSport: ActiveSport,
    val sports: List<SportInfo>,
) {
    data class ActiveSport(
        val profileId: String,
        val sportCode: String,
        val tierId: Int,
        val lp: Int,
        val minLp: Int,
        val maxLp: Int,
        val wins: Int,
        val losses: Int,
    ) {
        companion object {
            fun from(
                u: UserSportProfile,
            ) = ActiveSport(
                profileId = u.id!!,
                sportCode = u.sport.code,
                tierId = u.tier.orderNo,
                lp = u.lp,
                minLp = u.tier.minLp,
                maxLp = u.tier.minLp,
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
                activeSportProfileId: String,
            ) = allProfiles
                .filter { userSportProfile ->
                    userSportProfile.id != activeSportProfileId
                }.map { userSportProfile ->
                    from(userSportProfile)
                }
        }
    }

    companion object {
        fun from(
            nickname: String,
            activeSport: UserSportProfile,
            allProfiles: List<UserSportProfile>
        ) = UserProfilesResponse(
            nickname = nickname,
            activeSport = ActiveSport.from(activeSport),
            sports = SportInfo.listForm(
                allProfiles = allProfiles,
                activeSportProfileId = activeSport.id!!
            )
        )
    }
}