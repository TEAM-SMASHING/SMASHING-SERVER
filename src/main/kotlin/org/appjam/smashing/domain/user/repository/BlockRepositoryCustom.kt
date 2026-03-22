package org.appjam.smashing.domain.user.repository

interface BlockRepositoryCustom {
    fun findAllRelatedBlockIds(userId: String): List<String>
}
