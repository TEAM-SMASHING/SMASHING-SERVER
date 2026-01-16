package org.appjam.smashing.domain.auth.dto.command

import org.appjam.smashing.domain.sport.enums.ExperienceRange
import org.appjam.smashing.domain.user.enums.Gender

data class SignUpRequestCommand(
    val kakaoId: String,
    val nickname: String,
    val gender: Gender,
    val openChatUrl: String,
    val sportCode: String,
    val experienceRange: ExperienceRange,
    val region: String,
)
