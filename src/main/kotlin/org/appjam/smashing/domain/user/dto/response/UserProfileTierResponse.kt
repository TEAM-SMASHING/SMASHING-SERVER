package org.appjam.smashing.domain.user.dto.response

data class UserProfileTierResponse(
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
                profileId: String,
                sportCode: String,
                tierId: Int,
                lp: Int,
                minLp: Int,
                maxLp: Int,
                wins: Int,
                losses: Int,
            ) = ActiveSport(
                profileId = profileId,
                sportCode = sportCode,
                tierId = tierId,
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
