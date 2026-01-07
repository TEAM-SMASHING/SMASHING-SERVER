package org.appjam.smashing.domain.auth.dto

data class SignUpRequestDto(
    val nickname: String,
    val gender: String,
    val openChatUrl: String,
    val sportCode: String,
    val tier: String,
    val region: String,
)
