package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.user.entity.UserSportProfile

data class UserProfileTierResponse(
    val activeProfile: ActiveProfile,
    val allProfiles: List<ProfileInfo>,
) {
    data class ActiveProfile(
        val profileId: String,
        val sportCode: String,
        val tierId: Long,
        val lp: Int,
        val minLp: Int,
        val maxLp: Int,
        val wins: Int,
        val losses: Int,
    ) {
        companion object {
            fun from(
                u: UserSportProfile
            ) = ActiveProfile(
                profileId = u.id!!,
                sportCode = u.sport.code,
                tierId = u.tier.id!!,
                lp = u.lp,
                minLp = u.tier.minLp,
                maxLp = u.tier.maxLp,
                wins = u.wins,
                losses = u.losses,
            )
        }
    }

    data class ProfileInfo(
        val profileId: String,
        val sportCode: String,
        val isActive: Boolean
    ) {
        companion object {
            fun from(
                isActive: Boolean,
                u: UserSportProfile
            ) = ProfileInfo(
                profileId = u.id!!,
                sportCode = u.sport.code,
                isActive = isActive,
            )

            fun listForm(
                allProfiles: List<UserSportProfile>,
                activeProfileId: String,
            ) = allProfiles.map { userSportProfile ->
                from(
                    isActive = userSportProfile.id == activeProfileId,
                    u = userSportProfile
                )
            }
        }
    }

    companion object {
        fun from(
            activeProfile: UserSportProfile,
            allProfiles: List<UserSportProfile>
        ) = UserProfileTierResponse(
            activeProfile = ActiveProfile.from(activeProfile),
            allProfiles = ProfileInfo.listForm(
                allProfiles = allProfiles,
                activeProfileId = activeProfile.id!!
            )
        )
    }
}
