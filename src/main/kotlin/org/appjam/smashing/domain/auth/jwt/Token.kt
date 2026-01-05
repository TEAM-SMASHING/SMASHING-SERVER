package org.appjam.smashing.domain.auth.jwt

data class TokenDto(
    val token: Token,
    val expireTime: ExpireTime,
) {
    data class Token(
        val access: String,
        val refresh: String,
    )

    data class ExpireTime(
        val access: Long,
        val refresh: Long,
    )

    companion object {
        fun of(
            accessToken: String,
            refreshToken: String,
            accessExpireTime: Long,
            refreshExpireTime: Long,
        ): TokenDto = TokenDto(
            token = Token(
                access = accessToken,
                refresh = refreshToken,
            ),
            expireTime = ExpireTime(
                access = accessExpireTime,
                refresh = refreshExpireTime,
            )
        )
    }
}
