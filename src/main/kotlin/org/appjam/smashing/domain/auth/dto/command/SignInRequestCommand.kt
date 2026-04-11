package org.appjam.smashing.domain.auth.dto.command

import org.appjam.smashing.domain.auth.enums.ProviderType

data class SignInRequestCommand(
    val idToken: String,
    val provider: ProviderType,
)
