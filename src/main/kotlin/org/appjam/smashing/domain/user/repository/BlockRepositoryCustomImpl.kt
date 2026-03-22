package org.appjam.smashing.domain.user.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import org.appjam.smashing.domain.user.entity.QUserBlock

class BlockRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
) : BlockRepositoryCustom {
    override fun findAllRelatedBlockIds(
        userId: String,
    ): List<String> {
        val block = QUserBlock.userBlock

        return queryFactory
            .select(
                block.blocker.id.`when`(userId).then(block.blockedUser.id)
                    .otherwise(block.blocker.id)
            )
            .from(block)
            .where(
                block.blocker.id.eq(userId)
                    .or(block.blockedUser.id.eq(userId))
            )
            .fetch()
            .distinct()
    }
}
