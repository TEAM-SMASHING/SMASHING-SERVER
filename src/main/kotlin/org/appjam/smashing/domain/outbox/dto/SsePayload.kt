package org.appjam.smashing.domain.outbox.dto

import org.appjam.smashing.domain.game.enums.GameSubmissionRejectReason
import org.appjam.smashing.domain.game.enums.GameStatus
import org.appjam.smashing.domain.notification.enums.NotificationType
import org.appjam.smashing.domain.outbox.enums.MatchingUpdateStatus
import org.appjam.smashing.domain.outbox.enums.SseEventType
import org.appjam.smashing.domain.tier.enums.TierCode
import org.appjam.smashing.domain.user.enums.Gender

sealed interface SsePayload {
    val type: String
}

/**
 * 매칭 관리 - 받은 요청
 * - 내가 상대에게 매칭을 신청한 순간 상대에게 카드 추가 SSE 이벤트 발행
 */
data class MatchingReceivedPayload(
    override val type: String = SseEventType.MATCHING_RECEIVED.eventName,
    val matchingId: String,
    val sportCode: String,
    val receiverProfileId: String,
    val requester: MatchingRequesterSummary,
) : SsePayload {

    data class MatchingRequesterSummary(
        val requesterProfileId: String, // TODO: 유저 정보 조회시 profileid 값 논의 필요
        val nickname: String,
        val gender: Gender,
        val tierCode: TierCode,
        val wins: Int,
        val losses: Int,
        val reviewCount: Long,
    )
}

/**
 * 매칭 관리 - 보낸 요청
 * - 내가 상대에게 매칭을 신청한 순간 나에게 카드 추가 SSE 이벤트 발행
 */
data class MatchingSentPayload(
    override val type: String = SseEventType.MATCHING_SENT.eventName,
    val matchingId: String,
    val sportCode: String,
    val receiverProfileId: String,
    val receiver: MatchingReceiverSummary,
) : SsePayload {

    data class MatchingReceiverSummary(
        val receiverProfileId: String,
        val nickname: String,
        val gender: Gender,
        val tierCode: TierCode,
        val wins: Int,
        val losses: Int,
        val reviewCount: Long,
    )
}

/**
 * 매칭 상태 변경
 * - 매칭 요청 취소 / 매칭 요청 거절 / 매칭 요청 수락 시 카드 삭제 SSE 이벤트 발행
 */
data class MatchingUpdatedPayload(
    override val type: String = SseEventType.MATCHING_UPDATED.eventName,
    val matchingId: String,
    val status: MatchingUpdateStatus,
) : SsePayload

/**
 * 게임 상태 변경
 * - 게임 결과 전송 시 게임 상태 변경 SSE 이벤트 발행
 */
data class GameUpdatedPayload(
    override val type: String = SseEventType.GAME_UPDATED.eventName,
    val gameId: String,
    val submissionId: String,
    val submissionAttemptNo: Int,
    val resultStatus: GameStatus,
) : SsePayload

/**
 * 리뷰(후기) 제출 알림 생성
 * - 상대가 나에게 후기를 남긴 순간 알림 생성
 */
data class ReviewReceivedNotificationCreatedPayload(
    override val type: String = SseEventType.REVIEW_RECEIVED_NOTIFICATION_CREATED.eventName,
    val notificationId: String,
    val notificationType: NotificationType,
    val notificationCreatedAt: String,
    val sportId: Long,
    val receiverProfileId: String,
    val gameId: String,
    val reviewId: String,
    val reviewer: ReviewerSummary,
) : SsePayload {

    data class ReviewerSummary(
        val userId: String,
        val nickname: String,
        val tierCode: TierCode,
    )
}

/**
 * 경기 결과 거절 알림 생성
 * - 상대가 나의 결과 제출을 거절한 순간 알림 생성
 */
data class GameResultRejectedNotificationCreatedPayload(
    override val type: String = SseEventType.GAME_RESULT_REJECTED_NOTIFICATION_CREATED.eventName,
    val notificationId: String,
    val notificationType: NotificationType,
    val notificationCreatedAt: String,
    val sportId: Long,
    val receiverProfileId: String,
    val gameId: String,
    val submissionId: String,
    val reason: GameSubmissionRejectReason,
    val rejector: RejectorSummary,
) : SsePayload {

    data class RejectorSummary(
        val userId: String,
        val nickname: String,
        val tierCode: TierCode,
    )
}
