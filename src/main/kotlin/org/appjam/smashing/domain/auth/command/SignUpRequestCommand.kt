package org.appjam.smashing.domain.auth.command

data class SignUpRequestCommand(
    val nickname: String,
    val gender: String,
    val openChatUrl: String,
    val sportCode: String,
    val tier: String,
    val region: String,
)
