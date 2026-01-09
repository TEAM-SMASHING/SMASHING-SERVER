package org.appjam.smashing.domain.auth.command

import org.appjam.smashing.domain.sport.enums.SportCode
import org.appjam.smashing.domain.sport.enums.TierType
import org.appjam.smashing.domain.user.enums.Gender

data class SignUpRequestCommand(
    val authId: String,
    val nickname: String,
    val gender: Gender,
    val openChatUrl: String,
    val sportCode: SportCode,
    val tier: TierType,
    val region: String,
)
