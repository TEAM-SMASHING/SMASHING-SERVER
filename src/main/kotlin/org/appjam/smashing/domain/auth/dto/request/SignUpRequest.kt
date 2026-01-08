package org.appjam.smashing.domain.auth.dto.request

data class SignUpRequest(
    val nickname: String,
    val gender: String,
    val openChatUrl: String,
    val sportCode: String,
    val tier: String,
    val region: String,
)
