package org.appjam.smashing.domain.auth.command

import org.appjam.smashing.domain.sport.enums.SportCode
import org.appjam.smashing.domain.sport.enums.TierType

data class SignUpRequestCommand(
    val authId: String,
    val nickname: String,
    val gender: String,
    val openChatUrl: String,
    val sportCode: SportCode,
    val tier: TierType,
    val region: String,
)
