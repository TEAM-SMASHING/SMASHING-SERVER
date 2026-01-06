package org.appjam.smashing.domain.outbox.dto

import org.appjam.smashing.domain.notification.enums.NotificationType
import org.appjam.smashing.domain.outbox.enums.MatchingUpdateStatus
import org.appjam.smashing.domain.outbox.enums.SseEventType
import org.appjam.smashing.domain.user.enums.Gender

sealed interface SsePayload {
    val type: String
}

/**
 * 매칭관리 - 받은 요청
 * - 상대가 나에게 매칭을 신청한 순간
 */
data class MatchingReceivedPayload(
    override val type: String = SseEventType.MATCHING_RECEIVED.eventName,
    val matchingId: String,
    val requester: MatchingRequesterSummary,
) : SsePayload {

    data class MatchingRequesterSummary(
        val userId: String,
        val nickname: String,
        val gender: Gender,
        val tierName: String,
        val wins: Int,
        val losses: Int,
        val reviewCount: Long,
    )
}

/**
 * 매칭관리 - 보낸 요청 / 매칭확정 / 요청삭제
 * - 상대가 내 요청을 ACCEPT/REJECT 하는 순간
 * - 상대가 보낸 요청을 CANCELLED 하는 순간
 */
data class MatchingUpdatedPayload(
    override val type: String = SseEventType.MATCHING_UPDATED.eventName,
    val matchingId: String,
    val status: MatchingUpdateStatus,
) : SsePayload

/**
 * 알림 생성
 * - 알림이 생성된 순간만 SSE로 푸시
 */
data class NotificationCreatedPayload(
    override val type: String = SseEventType.NOTIFICATION_CREATED.eventName,
    val notificationId: String,
    val notificationType: NotificationType,
    val targetId: String? = null,           // matchingId/gameId/reviewId 등
) : SsePayload
