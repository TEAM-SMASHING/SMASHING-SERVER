package org.appjam.smashing.domain.auth.dto.command

data class TokenReissueCommand(
    val refreshToken: String,
)
