package org.appjam.smashing.domain.user.repository

import org.appjam.smashing.domain.user.entity.Block
import org.appjam.smashing.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface BlockRepository : JpaRepository<Block, String>, BlockRepositoryCustom {
    fun existsByBlockerAndBlockedUser(blocker: User, blockedUser: User): Boolean
}
