package org.appjam.smashing.domain.auth.dto.response

data class TokenReissueResponse(
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        fun from(
            accessToken: String,
            refreshToken: String,
        ) = TokenReissueResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}
