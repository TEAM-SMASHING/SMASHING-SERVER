package org.appjam.smashing.domain.auth.social

import org.appjam.smashing.domain.auth.enums.ProviderType
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oidc")
data class OidcProperties(
    val kakaoClientId: String,
    val appleClientId: String,
) {
    fun getClientId(providerType: ProviderType): String = when (providerType) {
        ProviderType.KAKAO -> kakaoClientId
        ProviderType.APPLE -> appleClientId
    }
}
