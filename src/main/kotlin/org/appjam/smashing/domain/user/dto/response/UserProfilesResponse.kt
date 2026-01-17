package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.appjam.smashing.domain.user.enums.Gender

data class UserProfilesResponse(
    val nickname: String,
    val gender: Gender,
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
        val reviews: Long,
    ) {
        companion object {
            fun from(
                u: UserSportProfile,
                reviews: Long,
            ) = ActiveProfile(
                profileId = u.id!!,
                sportCode = u.sport.code,
                tierId = u.tier.id!!,
                lp = u.lp,
                minLp = u.tier.minLp,
                maxLp = u.tier.maxLp,
                wins = u.wins,
                losses = u.losses,
                reviews = reviews,
            )
        }
    }

    data class ProfileInfo(
        val profileId: String,
        val sportCode: String,
        val isActive: Boolean,
    ) {
        companion object {
            fun from(
                isActive: Boolean,
                u: UserSportProfile,
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
            nickname: String,
            gender: Gender,
            reviews: Long,
            activeProfile: UserSportProfile,
            allProfiles: List<UserSportProfile>
        ) = UserProfilesResponse(
            nickname = nickname,
            gender = gender,
            activeProfile = ActiveProfile.from(
                u = activeProfile,
                reviews = reviews,
            ),
            allProfiles = ProfileInfo.listForm(
                allProfiles = allProfiles,
                activeProfileId = activeProfile.id!!,
            )
        )
    }
}