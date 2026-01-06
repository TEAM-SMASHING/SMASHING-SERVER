package org.appjam.smashing.domain.auth.jwt

data class TokenDto(
    val accessToken: Token,
    val refreshToken: Token,
) {
    data class Token(
        val token: String,
        val expiredAt: Long,
    ) {
        companion object {
            fun of(
                token: String,
                expiredAt: Long
            ): Token = Token(
                token = token,
                expiredAt = expiredAt
            )
        }
    }

    companion object {
        fun of(
            accessToken: String,
            accessTokenExpiredAt: Long,
            refreshToken: String,
            refreshTokenExpiredAt: Long,
        ): TokenDto = TokenDto(
            accessToken = Token(
                token = accessToken,
                expiredAt = accessTokenExpiredAt,
            ),
            refreshToken = Token(
                token = refreshToken,
                expiredAt = refreshTokenExpiredAt,
            )
        )
    }
}
