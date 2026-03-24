package org.appjam.smashing.domain.user.repository

import org.appjam.smashing.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, String> {
    fun findByKakaoId(kakaoId: String): User?
    fun existsByKakaoId(kakaoId: String): Boolean
    fun existsByNickname(nickname: String): Boolean
    fun existsByOpenchatUrl(openChatUrl: String): Boolean

    @Query(
        value = """
            SELECT * 
            FROM user 
            WHERE id = :id
       """,
        nativeQuery = true
    )
    fun findByIdIncludingDeleted(
        id: String,
    ): User?
}
