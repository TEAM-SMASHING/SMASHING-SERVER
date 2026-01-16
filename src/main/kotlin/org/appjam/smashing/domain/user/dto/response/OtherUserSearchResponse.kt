package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.user.dto.projection.OtherUserSearchProjection

data class OtherUserSearchResponse(
    val users: List<SearchUser>,
) {
    data class SearchUser(
        val userId: String,
        val nickname: String,
    ) {
        companion object {
            fun from(
                userId: String,
                nickname: String,
            ) = SearchUser(
                userId = userId,
                nickname = nickname,
            )

            fun listForm(
                users: List<OtherUserSearchProjection>
            ) = users.map { otherUser ->
                from(
                    userId = otherUser.userId,
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
