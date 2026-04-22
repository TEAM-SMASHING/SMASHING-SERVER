package org.appjam.smashing.domain.auth.dto.response

import org.appjam.smashing.domain.auth.enums.ProviderType

data class SocialType(
    val provider: ProviderType,
    val socialId: String,
)
