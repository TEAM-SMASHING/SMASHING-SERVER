package org.appjam.smashing.domain.notification.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.appjam.smashing.domain.notification.dto.projection.NotificationSummaryProjection
import org.appjam.smashing.domain.notification.dto.projection.QNotificationSummaryProjection
import org.appjam.smashing.domain.notification.entity.QNotification.Companion.notification
import org.appjam.smashing.domain.notification.entity.QNotificationTemplate.Companion.notificationTemplate
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorPageResponse
import org.appjam.smashing.global.util.CursorCodec
import java.time.OffsetDateTime

class NotificationRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
    private val cursorCodec: CursorCodec,
) : NotificationRepositoryCustom {

    override fun fetchMyNotificationPage(
        userId: String,
        request: CommonCursorRequest,
        snapshotAt: OffsetDateTime,
    ): CursorPageResponse<NotificationSummaryProjection> {

        val size = request.size.coerceIn(1, 50).toInt()
        val cursor = cursorCodec.decode(request.cursor)

        val where = BooleanBuilder()
            .and(notification.user.id.eq(userId))
            .and(notification.createdAt.loe(snapshotAt.toLocalDateTime()))

        if (cursor != null) {
            where.and(notification.id.lt(cursor.id))
        }

        val fetched = queryFactory
            .select(
                QNotificationSummaryProjection(
                    notification.id,
                    notificationTemplate.type,
                    notificationTemplate.title,
                    notificationTemplate.content,
                    notification.params,
                    notification.linkUrl,
                    notification.isRead,
                    notification.createdAt,
                    notification.senderNickname,
                    notification.receiverProfileId,
                    notification.receiverSportId,

                )
            )
            .from(notification)
            .join(notification.notificationTemplate, notificationTemplate)
            .where(where)
            .orderBy(notification.id.desc())
            .limit((size + 1).toLong())
            .fetch()

        return CursorPageResponse.create(
            snapshotAt = snapshotAt,
            fetched = fetched,
            pageSize = size,
            cursorCodec = cursorCodec,
        )
    }
}
