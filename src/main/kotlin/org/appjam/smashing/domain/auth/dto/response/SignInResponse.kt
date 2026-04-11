package org.appjam.smashing.domain.auth.dto.response

data class SignInResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val socialId: String,
    val userId: String?,
    val nickname: String?,
) {
    fun isCompletedSignUp(): Boolean = accessToken != null && refreshToken != null

    companion object {
        fun from(
            accessToken: String? = null,
            refreshToken: String? = null,
            socialId: String,
            userId: String? = null,
            nickname: String? = null,
        ) = SignInResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            socialId = socialId,
            userId = userId,
            nickname = nickname,
        )
    }
}
