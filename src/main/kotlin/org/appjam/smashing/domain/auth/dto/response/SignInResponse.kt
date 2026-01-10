package org.appjam.smashing.domain.auth.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore

data class SignInResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val authId: String,
) {
    @JsonIgnore
    fun isCompletedSignUp(): Boolean = accessToken != null && refreshToken != null
}
