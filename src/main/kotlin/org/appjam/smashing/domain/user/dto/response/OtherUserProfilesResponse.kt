package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.tier.enums.TierCode
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.appjam.smashing.domain.user.enums.Gender

data class OtherUserProfilesResponse(
    val nickname: String,
    val gender: Gender,
    val canChallenge: Boolean,
    val canAccept: Boolean,
    val matchingId: String?,
    val selectedProfile: SelectedProfile,
    val allProfiles: List<ProfileInfo>,
) {
    data class SelectedProfile(
        val profileId: String,
        val sportCode: String,
        val tierCode: TierCode,
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
            ) = SelectedProfile(
                profileId = u.id!!,
                sportCode = u.sport.code,
                tierCode = u.tier.code,
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
            reviews: Long,
            canChallenge: Boolean,
            canAccept: Boolean,
            matchingId: String,
            selectedProfile: UserSportProfile,
            allProfiles: List<UserSportProfile>,
        ) = OtherUserProfilesResponse(
            nickname = nickname,
            gender = gender,
            canChallenge = canChallenge,
            canAccept = canAccept,
            matchingId = matchingId,
            selectedProfile = SelectedProfile.from(
                u = selectedProfile,
                reviews = reviews,
            ),
            allProfiles = ProfileInfo.listForm(
                allProfiles = allProfiles,
                selectedProfileId = selectedProfile.id!!
            )
        )
    }
}
