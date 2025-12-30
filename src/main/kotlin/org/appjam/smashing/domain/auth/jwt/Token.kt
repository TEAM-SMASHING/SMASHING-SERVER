package org.appjam.smashing.domain.auth.jwt

data class Token(
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        fun of(
            accessToken: String,
            refreshToken: String,
        ) = Token(accessToken, refreshToken)
    }
}
