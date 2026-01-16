package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile

data class UserWithActiveProfile(
    val user: User,
    val activeProfile: UserSportProfile,
)
