package org.appjam.smashing.domain.auth.dto.response

data class SignInResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val authId: String?,
) {
    fun isCompletedSignUp(): Boolean = accessToken != null && refreshToken != null
}
