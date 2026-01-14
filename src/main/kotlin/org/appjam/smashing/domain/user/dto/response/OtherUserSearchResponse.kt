package org.appjam.smashing.domain.user.dto.response

data class OtherUserSearchResponse(
    val users: SearchUser
) {
    data class SearchUser(
        val userId: String,
        val nickname: String,
    )
}