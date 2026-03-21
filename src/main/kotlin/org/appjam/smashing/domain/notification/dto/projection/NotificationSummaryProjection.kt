package org.appjam.smashing.domain.notification.dto.projection

import com.querydsl.core.annotations.QueryProjection
import org.appjam.smashing.domain.notification.enums.NotificationType
import org.appjam.smashing.global.common.dto.CursorKey
import org.appjam.smashing.global.util.TimeUtils
import java.time.LocalDateTime
import java.time.OffsetDateTime

@QueryProjection
data class NotificationSummaryProjection(
    val notificationId: String,
    val type: NotificationType,
    val title: String,
    val content: String,
    val linkUrl: String?,
    val isRead: Boolean,
    val createdAtLdt: LocalDateTime,
    val senderProfileId: String?,
) : CursorKey {

    override val cursorId: String
        get() = notificationId

    val createdAt: OffsetDateTime
        get() = TimeUtils.toOffsetDateTime(createdAtLdt)
}
