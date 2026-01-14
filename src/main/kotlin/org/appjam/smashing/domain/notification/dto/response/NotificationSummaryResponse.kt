package org.appjam.smashing.domain.notification.dto.response

import org.appjam.smashing.domain.notification.dto.projection.NotificationSummaryProjection
import org.appjam.smashing.domain.notification.enums.NotificationType
import org.appjam.smashing.global.common.components.NotificationContentRenderer
import java.time.OffsetDateTime

data class NotificationSummaryResponse(
    val notificationId: String,
    val type: NotificationType,
    val title: String,
    val content: String,
    val linkUrl: String,
    val isRead: Boolean,
    val createdAt: OffsetDateTime,
    val receiverProfileId: String,
    val receiverSportId: Long,
) {
    companion object {
        fun from(
            p: NotificationSummaryProjection,
            renderer: NotificationContentRenderer,
        ) = NotificationSummaryResponse(
            notificationId = p.notificationId,
            type = p.type,
            title = renderer.render(
                templateContent = p.templateTitle,
                paramsJson = p.params,
            ),
            content = renderer.render(
                templateContent = p.templateContent,
                paramsJson = p.params,
            ),
            linkUrl = p.linkUrl,
            isRead = p.isRead,
            createdAt = p.createdAt,
            receiverProfileId = p.receiverProfileId,
            receiverSportId = p.receiverSportId,
        )

        fun from(
            list: List<NotificationSummaryProjection>,
            renderer: NotificationContentRenderer,
        ) = list.map { from(it, renderer) }
    }
}
