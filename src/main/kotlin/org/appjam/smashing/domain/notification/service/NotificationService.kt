package org.appjam.smashing.domain.notification.service

import org.appjam.smashing.domain.notification.entity.Notification
import org.appjam.smashing.domain.notification.enums.NotificationType
import org.appjam.smashing.domain.notification.repository.NotificationRepository
import org.appjam.smashing.domain.notification.repository.NotificationTemplateRepository
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val notificationTemplateRepository: NotificationTemplateRepository,
) {

    fun createMatchingRequested(
        receiver: User,
        requesterProfile: UserSportProfile,
    ): Notification {
        val template = notificationTemplateRepository.findByType(NotificationType.MATCHING_REQUESTED)
            ?: throw CustomException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND)

        return notificationRepository.save(
            Notification.createMatchingRequested(
                receiver = receiver,
                template = template,
                requesterProfile = requesterProfile,
            )
        )
    }

    fun createMatchingAccepted(
        receiver: User,
        acceptorProfile: UserSportProfile,
    ): Notification {
        val template = notificationTemplateRepository.findByType(NotificationType.MATCHING_ACCEPTED)
            ?: throw CustomException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND)

        return notificationRepository.save(
            Notification.createMatchingRequestAccepted(
                receiver = receiver,
                template = template,
                acceptorProfile = acceptorProfile,
            )
        )
    }

    fun createMatchingResultSubmitted(
        receiver: User,
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
                    template = template,
                    gameId = gameId,
                    submissionId = submissionId,
                    rejectorNickname = rejectorNickname,
                    rejectorTierId = rejectorTierId,
                )

            NotificationType.RESULT_REJECTED_WIN_LOSE_REVERSED ->
                Notification.createResultRejectedWinLoseReversed(
                    receiver = receiver,
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
}
