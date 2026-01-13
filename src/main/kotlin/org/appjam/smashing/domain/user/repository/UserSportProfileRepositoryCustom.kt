package org.appjam.smashing.domain.user.repository

import org.appjam.smashing.domain.user.entity.UserSportProfile

interface UserSportProfileRepositoryCustom {
    fun findRandomCandidates(
        region: String,
        sportId: Long,
        excludeUserId: String,
        myLp: Int,
        lpThreshold: Int,
        limit: Long,
    ): List<UserSportProfile>
}
