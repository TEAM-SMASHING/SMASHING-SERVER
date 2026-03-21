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

        // 내가 차단한 사람들의 ID
        val blockedByUser = queryFactory
            .select(block.blockedUser.id)
            .from(block)
            .where(block.blocker.id.eq(userId))
            .fetch()

        // 나를 차단한 사람들의 ID
        val blockedByOtherUser = queryFactory
            .select(block.blocker.id)
            .from(block)
            .where(block.blockedUser.id.eq(userId))
            .fetch()

        return (blockedByUser + blockedByOtherUser).distinct()
    }
}
