package org.appjam.smashing.domain.user.dto.response

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
                profileId: String,
                sportCode: String,
                tier: Int,
                lp: Int,
                minLp: Int,
                maxLp: Int,
                wins: Int,
                losses: Int,
            ) = SelectedSport(
                profileId = profileId,
                sportCode = sportCode,
                tier = tier,
                lp = lp,
                minLp = minLp,
                maxLp = maxLp,
                wins = wins,
                losses = losses,
            )
        }
    }

    data class SportInfo(
        val profileId: String,
        val sportCode: String,
    ) {
        companion object {
            fun from(
                profileId: String,
                sportCode: String,
            ) = SportInfo(
                profileId = profileId,
                sportCode = sportCode,
            )
        }
    }
}
