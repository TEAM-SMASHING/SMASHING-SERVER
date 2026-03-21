package org.appjam.smashing.domain.user.repository

import com.querydsl.core.types.dsl.Expressions
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
                Expressions.stringTemplate(
                    "case when {0} = {1} then {2} else {3} end",
                    block.blocker.id,
                    userId,
                    block.blockedUser.id,
                    block.blocker.id
                )
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
