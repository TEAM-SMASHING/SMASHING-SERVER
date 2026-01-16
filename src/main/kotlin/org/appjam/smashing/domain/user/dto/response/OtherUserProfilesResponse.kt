package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.appjam.smashing.domain.user.enums.Gender

data class OtherUserProfilesResponse(
    val nickname: String,
    val gender: Gender,
    val selectedProfile: SelectedProfile,
    val allProfiles: List<ProfileInfo>,
) {
    data class SelectedProfile(
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
                u: UserSportProfile,
            ) = SelectedProfile(
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
        val isSelected: Boolean,
    ) {
        companion object {
            fun from(
                isSelected: Boolean,
                u: UserSportProfile,
            ) = ProfileInfo(
                profileId = u.id!!,
                sportCode = u.sport.code,
                isSelected = isSelected
            )

            fun listForm(
                allProfiles: List<UserSportProfile>,
                selectedProfileId: String,
            ) = allProfiles.map { userSportProfile ->
                from(
                    isSelected = userSportProfile.id == selectedProfileId,
                    u = userSportProfile,
                )
            }
        }
    }

    companion object {
        fun from(
            nickname: String,
            gender: Gender,
            selectedProfile: UserSportProfile,
            allProfiles: List<UserSportProfile>,
        ) = OtherUserProfilesResponse(
            nickname = nickname,
            gender = gender,
            selectedProfile = SelectedProfile.from(selectedProfile),
            allProfiles = ProfileInfo.listForm(
                allProfiles = allProfiles,
                selectedProfileId = selectedProfile.id!!
            )
        )
    }
}
