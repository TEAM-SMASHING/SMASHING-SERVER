package org.appjam.smashing.domain.auth.command.response

import org.appjam.smashing.domain.auth.dto.response.SignUpResponse
import org.appjam.smashing.global.auth.jwt.dto.TokenDto

data class SignUpResponseCommand(
    val token: TokenDto,
) {
    companion object {
        fun SignUpResponseCommand.toDto(): SignUpResponse = SignUpResponse(
            accessToken = token.accessToken.token,
            refreshToken = token.refreshToken.token,
        )
    }
}
