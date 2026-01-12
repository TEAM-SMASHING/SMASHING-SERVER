package org.appjam.smashing.domain.notification.dto.response

import org.appjam.smashing.domain.notification.dto.projection.NotificationSummaryProjection
import org.appjam.smashing.domain.notification.enums.NotificationType
import java.time.OffsetDateTime

data class NotificationSummaryResponse(
    val notificationId: String,
    val type: NotificationType,
    val templateTitle: String,
    val templateContent: String,
    val params: String,
    val linkUrl: String,
    val isRead: Boolean,
    val createdAt: OffsetDateTime,
    val receiverProfileId: String,
    val receiverSportId: Long,
) {
    companion object {
        fun from(
            p: NotificationSummaryProjection
        ) = NotificationSummaryResponse(
                notificationId = p.notificationId,
                type = p.type,
                templateTitle = p.templateTitle,
                templateContent = p.templateContent,
                params = p.params,
                linkUrl = p.linkUrl,
                isRead = p.isRead,
                createdAt = p.createdAt,
                receiverProfileId = p.receiverProfileId,
                receiverSportId = p.receiverSportId,
            )

        fun from(
            list: List<NotificationSummaryProjection>
        ) = list.map(::from)
    }
}
