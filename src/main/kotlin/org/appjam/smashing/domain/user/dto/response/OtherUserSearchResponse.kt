package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.user.dto.projection.OtherUserSearchProjection

data class OtherUserSearchResponse(
    val users: List<SearchUser>,
) {
    data class SearchUser(
        val userProfileId: String,
        val nickname: String,
    ) {
        companion object {
            fun from(
                userProfileId: String,
                nickname: String,
            ) = SearchUser(
                userProfileId = userProfileId,
                nickname = nickname,
            )

            fun listForm(
                users: List<OtherUserSearchProjection>
            ) = users.map { otherUser ->
                from(
                    userProfileId = otherUser.userProfileId,
                    nickname = otherUser.nickname,
                )
            }
        }
    }

    companion object {
        fun from(
            users: List<OtherUserSearchProjection>,
        ) = OtherUserSearchResponse(
            users = SearchUser.listForm(users)
        )
    }
}
