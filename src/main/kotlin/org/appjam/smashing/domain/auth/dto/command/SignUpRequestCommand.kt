package org.appjam.smashing.domain.auth.dto.command

import org.appjam.smashing.domain.user.enums.Gender

data class SignUpRequestCommand(
    val authId: String,
    val nickname: String,
    val gender: Gender,
    val openChatUrl: String,
    val sportCode: String,
    val tier: String,
    val region: String,
)
