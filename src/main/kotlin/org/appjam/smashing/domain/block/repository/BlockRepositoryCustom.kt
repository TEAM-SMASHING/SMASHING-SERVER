package org.appjam.smashing.domain.block.repository

interface BlockRepositoryCustom {
    fun findAllRelatedBlockIds(userId: String): List<String>
}
