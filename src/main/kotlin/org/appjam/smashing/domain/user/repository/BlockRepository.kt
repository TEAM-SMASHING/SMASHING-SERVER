package org.appjam.smashing.domain.user.repository

import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserBlock
import org.springframework.data.jpa.repository.JpaRepository

interface BlockRepository : JpaRepository<UserBlock, String>, BlockRepositoryCustom {
    fun existsByBlockerAndBlockedUser(blocker: User, blockedUser: User): Boolean
}
