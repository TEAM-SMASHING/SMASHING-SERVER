package org.appjam.smashing.domain.user.repository

import org.appjam.smashing.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String> {
    fun findByKakaoId(kakaoId: String): User?
}
