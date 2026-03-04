package org.appjam.smashing.domain.auth.dto.response

data class SignUpResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String?,
    val nickname: String,
) {
    companion object {
        fun from(
            accessToken: String,
            refreshToken: String,
            userId: String?,
            nickname: String,
        ) = SignUpResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId,
            nickname = nickname,
        )
    }
}
