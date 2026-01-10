package org.appjam.smashing.domain.user.dto.response

data class UserProfileTierResponse(
    val activeSport: ActiveSport,
    val sports: List<SportInfo>,
) {
    data class ActiveSport(
        val profileId: String,
        val sportCode: String,
        val tier: String,
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
                tier: String,
                lp: Int,
                minLp: Int,
                maxLp: Int,
                wins: Int,
                losses: Int,
            ) = ActiveSport(
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
