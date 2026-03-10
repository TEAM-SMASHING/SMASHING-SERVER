package org.appjam.smashing.domain.notification.service

import org.appjam.smashing.domain.game.entity.Game
import org.appjam.smashing.domain.game.entity.GameResultSubmission
import org.appjam.smashing.domain.notification.dto.response.NotificationSummaryResponse
import org.appjam.smashing.domain.notification.entity.Notification
import org.appjam.smashing.domain.notification.enums.NotificationType
import org.appjam.smashing.domain.notification.repository.NotificationRepository
import org.appjam.smashing.domain.notification.repository.NotificationTemplateRepository
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.appjam.smashing.global.common.components.NotificationContentRenderer
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorResponse
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.appjam.smashing.global.util.TimeUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val notificationContentRenderer: NotificationContentRenderer,
    private val notificationRepository: NotificationRepository,
    private val notificationTemplateRepository: NotificationTemplateRepository,
) {

    fun createMatchingRequested(
        receiver: User,
        receiverProfile: UserSportProfile,
        requesterProfile: UserSportProfile,
    ) {
        notificationRepository.save(
            Notification.createMatchingRequested(
                receiver = receiver,
                receiverProfile = receiverProfile,
                requesterProfile = requesterProfile,
            )
        )
    }

    fun createMatchingAccepted(
        receiver: User,
        receiverProfile: UserSportProfile,
        acceptorProfile: UserSportProfile,
    ) {
        notificationRepository.save(
            Notification.createMatchingAccepted(
                receiver = receiver,
                receiverProfile = receiverProfile,
                acceptorProfile = acceptorProfile,
            )
        )
    }

    fun createMatchingResultSubmitted(
        receiver: User,
        receiverProfile: UserSportProfile,
        submitterNickname: String,
        game : Game,
        submission : GameResultSubmission,
    ): Notification {
        val template = notificationTemplateRepository.findByType(NotificationType.MATCHING_RESULT_SUBMITTED)
            ?: throw CustomException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND)

        return notificationRepository.save(
            Notification.createMatchingResultSubmitted(
                receiver = receiver,
                receiverProfile = receiverProfile,
                template = template,
                submitterNickname = submitterNickname,
                game = game,
                submission = submission,
            )
        )
    }

    fun createReviewReceived(
        receiver: User,
        receiverProfile: UserSportProfile,
        reviewId: String,
        reviewerNickname: String,
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
            )
        )
    }

    fun createResultRejected(
        receiver: User,
        receiverProfile: UserSportProfile,
        notificationType: NotificationType,
        rejectorNickname: String,
    ): Notification {
        val template = notificationTemplateRepository.findByType(notificationType)
            ?: throw CustomException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND)

        val notification = when (notificationType) {
            NotificationType.RESULT_REJECTED_SCORE_MISMATCH ->
                Notification.createResultRejectedScoreMismatch(
                    receiver = receiver,
                    receiverProfile = receiverProfile,
                    template = template,
                    rejectorNickname = rejectorNickname,
                )

            NotificationType.RESULT_REJECTED_WIN_LOSE_REVERSED ->
                Notification.createResultRejectedWinLoseReversed(
                    receiver = receiver,
                    receiverProfile = receiverProfile,
                    template = template,
                    rejectorNickname = rejectorNickname,
                )

            NotificationType.RESULT_REJECTED_SCORE_AND_WIN_LOSE_MISMATCH ->
                Notification.createResultRejectedScoreAndWinLoseMismatch(
                    receiver = receiver,
                    receiverProfile = receiverProfile,
                    template = template,
                    rejectorNickname = rejectorNickname,
                )

            NotificationType.RESULT_REJECTED_GAME_NOT_PLAYED_YET ->
                Notification.createResultRejectedGameNotPlayedYet(
                    receiver = receiver,
                    receiverProfile = receiverProfile,
                    template = template,
                    rejectorNickname = rejectorNickname,
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
            results = NotificationSummaryResponse.from(page.results, notificationContentRenderer),
            nextCursor = page.nextCursor,
            hasNext = page.hasNext,
        )
    }
}
