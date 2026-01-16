package org.appjam.smashing.domain.notification.service

import org.appjam.smashing.domain.notification.dto.response.NotificationSummaryResponse
import org.appjam.smashing.domain.notification.entity.Notification
import org.appjam.smashing.domain.notification.enums.NotificationType
import org.appjam.smashing.domain.notification.repository.NotificationRepository
import org.appjam.smashing.domain.notification.repository.NotificationTemplateRepository
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorResponse
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.appjam.smashing.global.util.TimeUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val notificationTemplateRepository: NotificationTemplateRepository,
) {

    fun createMatchingRequested(
        receiver: User,
        receiverProfile: UserSportProfile,
        requesterProfile: UserSportProfile,
    ): Notification {
        val template = notificationTemplateRepository.findByType(NotificationType.MATCHING_REQUESTED)
            ?: throw CustomException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND)

        return notificationRepository.save(
            Notification.createMatchingRequested(
                receiver = receiver,
                receiverProfile = receiverProfile,
                template = template,
                requesterProfile = requesterProfile,
            )
        )
    }

    fun createMatchingAccepted(
        receiver: User,
        receiverProfile: UserSportProfile,
        acceptorProfile: UserSportProfile,
    ): Notification {
        val template = notificationTemplateRepository.findByType(NotificationType.MATCHING_ACCEPTED)
            ?: throw CustomException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND)

        return notificationRepository.save(
            Notification.createMatchingRequestAccepted(
                receiver = receiver,
                receiverProfile = receiverProfile,
                template = template,
                acceptorProfile = acceptorProfile,
            )
        )
    }

    fun createMatchingResultSubmitted(
        receiver: User,
        receiverProfile: UserSportProfile,
        gameId: String,
        submissionId: String,
        submitterNickname: String,
        submitterTierId: Long,
    ): Notification {
        val template = notificationTemplateRepository.findByType(NotificationType.MATCHING_RESULT_SUBMITTED)
            ?: throw CustomException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND)

        return notificationRepository.save(
            Notification.createMatchingResultSubmitted(
                receiver = receiver,
                receiverProfile = receiverProfile,
                template = template,
                gameId = gameId,
                submissionId = submissionId,
                submitterNickname = submitterNickname,
                submitterTierId = submitterTierId,
            )
        )
    }

    fun createReviewReceived(
        receiver: User,
        receiverProfile: UserSportProfile,
        reviewId: String,
        reviewerNickname: String,
        reviewerTierId: Long,
        gameId: String,
    ): Notification {
        val template = notificationTemplateRepository.findByType(NotificationType.REVIEW_RECEIVED)
            ?: throw CustomException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND)

        return notificationRepository.save(
            Notification.createReviewReceived(
                receiver = receiver,
                receiverProfile = receiverProfile,
                template = template,
                reviewId = reviewId,
                reviewerNickname = reviewerNickname,
                reviewerTierId = reviewerTierId,
                gameId = gameId,
            )
        )
    }

    fun createResultRejected(
        receiver: User,
        receiverProfile: UserSportProfile,
        notificationType: NotificationType,
        gameId: String,
        submissionId: String,
        rejectorNickname: String,
        rejectorTierId: Long,
    ): Notification {
        val template = notificationTemplateRepository.findByType(notificationType)
            ?: throw CustomException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND)

        val notification = when (notificationType) {
            NotificationType.RESULT_REJECTED_SCORE_MISMATCH ->
                Notification.createResultRejectedScoreMismatch(
                    receiver = receiver,
                    receiverProfile = receiverProfile,
                    template = template,
                    gameId = gameId,
                    submissionId = submissionId,
                    rejectorNickname = rejectorNickname,
                    rejectorTierId = rejectorTierId,
                )

            NotificationType.RESULT_REJECTED_WIN_LOSE_REVERSED ->
                Notification.createResultRejectedWinLoseReversed(
                    receiver = receiver,
                    receiverProfile = receiverProfile,
                    template = template,
                    gameId = gameId,
                    submissionId = submissionId,
                    rejectorNickname = rejectorNickname,
                    rejectorTierId = rejectorTierId,
                )

            else -> throw CustomException(ErrorCode.NOTIFICATION_RESULT_REJECTED_TYPE_MISMATCH)
        }

        return notificationRepository.save(notification)
    }

    @Transactional
    fun markAsRead(
        userId: String,
        notificationId: String,
    ) {
        val notification = notificationRepository.findByIdFetchUser(notificationId)
            ?: throw CustomException(ErrorCode.NOTIFICATION_NOT_FOUND)

        // 본인 알림인지 검증
        if (notification.user.id != userId) {
            throw CustomException(ErrorCode.NOTIFICATION_FORBIDDEN)
        }

        // 읽음 처리
        notification.markAsRead()
    }

    @Transactional(readOnly = true)
    fun getMyNotifications(
        userId: String,
        request: CommonCursorRequest,
    ): CursorResponse<NotificationSummaryResponse> {
        // 스냅샷 시각 설정
        val snapshotAt = request.snapshotAt ?: TimeUtils.nowOffsetDateTime()

        // 알림 페이지 조회
        val page = notificationRepository.fetchMyNotificationPage(
            userId = userId,
            request = request,
            snapshotAt = snapshotAt,
        )

        // 응답 반환
        return CursorResponse(
            snapshotAt = page.snapshotAt,
            results = NotificationSummaryResponse.from(page.results),
            nextCursor = page.nextCursor,
            hasNext = page.hasNext,
        )
    }
}
