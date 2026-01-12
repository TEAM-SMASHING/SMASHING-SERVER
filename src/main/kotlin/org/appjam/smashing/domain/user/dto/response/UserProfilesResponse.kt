package org.appjam.smashing.domain.user.dto.response

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

    }

    data class SportInfo(
        val profileId: String,
        val sportCode: String,
    ) {

    }
}