package org.appjam.smashing.domain.user.dto.response

import org.appjam.smashing.domain.user.entity.UserSportProfile

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
                users: List<UserSportProfile>
            ) = users.map { user ->
                from(
                    userId = user.id!!,
                    nickname = user.user.nickname,
                )
            }
        }
    }

    companion object {
        fun from(
            users: List<UserSportProfile>,
        ) = OtherUserSearchResponse(
            users = SearchUser.listForm(users)
        )
    }
}
