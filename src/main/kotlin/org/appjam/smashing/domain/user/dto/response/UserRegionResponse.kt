package org.appjam.smashing.domain.user.dto.response

data class UserRegionResponse(
    val region: String,
) {
    companion object {
        fun from(
            region: String
        ) = UserRegionResponse(
            region = region,
        )
    }
}
