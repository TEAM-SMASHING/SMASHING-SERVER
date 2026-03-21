package org.appjam.smashing.domain.notification.dto.response

import org.appjam.smashing.domain.notification.dto.projection.NotificationSummaryProjection
import org.appjam.smashing.domain.notification.enums.NotificationType
import java.time.OffsetDateTime

data class NotificationSummaryResponse(
    val notificationId: String,
    val type: NotificationType,
    val title: String,
    val content: String,
    val linkUrl: String?,
    val isRead: Boolean,
    val createdAt: OffsetDateTime,
    val senderProfileId: String?,
) {
    companion object {
        fun from(
            results: List<NotificationSummaryProjection>
        ): List<NotificationSummaryResponse> {
            return results.map {
                NotificationSummaryResponse(
                    notificationId = it.notificationId,
                    type = it.type,
                    title = it.title,
                    content = it.content,
                    linkUrl = it.linkUrl,
                    isRead = it.isRead,
                    createdAt = it.createdAt,
                    senderProfileId = it.senderProfileId,
                )
            }
        }
    }
}
