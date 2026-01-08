package org.appjam.smashing.domain.auth.command.reqeust

import org.appjam.smashing.domain.auth.dto.request.SignUpRequest

data class SignUpRequestCommand(
    val nickname: String,
    val gender: String,
    val openChatUrl: String,
    val sportCode: String,
    val tier: String,
    val region: String,
) {
    companion object {
        fun SignUpRequest.toCommand(): SignUpRequestCommand = SignUpRequestCommand(
            nickname = nickname,
            gender = gender,
            openChatUrl = openChatUrl,
            sportCode = sportCode,
            tier = tier,
            region = region,
        )
    }
}
