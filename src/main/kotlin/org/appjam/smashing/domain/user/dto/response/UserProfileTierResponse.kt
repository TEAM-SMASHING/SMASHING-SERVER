package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.tier.enums.TierCode
import org.appjam.smashing.domain.user.entity.UserSportProfile

data class UserProfileTierResponse(
    val region: String,
    val nickname: String,
    val activeProfile: ActiveProfile,
    val allProfiles: List<ProfileInfo>,
) {
    data class ActiveProfile(
        val profileId: String,
        val sportCode: String,
        val tierCode: TierCode,
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
                tierCode = u.tier.code,
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
            region: String,
            nickname: String,
            activeProfile: UserSportProfile,
            allProfiles: List<UserSportProfile>
        ) = UserProfileTierResponse(
            region = region,
            nickname = nickname,
            activeProfile = ActiveProfile.from(activeProfile),
            allProfiles = ProfileInfo.listForm(
                allProfiles = allProfiles,
                activeProfileId = activeProfile.id!!
            )
        )
    }
}
