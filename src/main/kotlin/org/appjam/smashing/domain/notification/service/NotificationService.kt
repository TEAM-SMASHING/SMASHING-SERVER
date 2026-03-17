package org.appjam.smashing.domain.notification.service

import org.appjam.smashing.domain.game.entity.Game
import org.appjam.smashing.domain.game.entity.GameResultSubmission
import org.appjam.smashing.domain.game.enums.GameSubmissionRejectReason
import org.appjam.smashing.domain.notification.dto.response.NotificationSummaryResponse
import org.appjam.smashing.domain.notification.entity.Notification
import org.appjam.smashing.domain.notification.repository.NotificationRepository
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

    fun createGameResultSubmitted(
        receiver: User,
        receiverProfile: UserSportProfile,
        submitterProfile: UserSportProfile,
        game: Game,
        submission: GameResultSubmission,
    ): Notification {
        return notificationRepository.save(
            Notification.createGameResultSubmitted(
                receiver = receiver,
                receiverProfile = receiverProfile,
                submitterProfile = submitterProfile,
                game = game,
                submission = submission,
            )
        )
    }

    fun createGameResultRejected(
        receiver: User,
        receiverProfile: UserSportProfile,
        rejectorProfile: UserSportProfile,
        reason: GameSubmissionRejectReason,
    ): Notification {
        return notificationRepository.save(
            Notification.createGameResultRejected(
                receiver = receiver,
                receiverProfile = receiverProfile,
                rejectorProfile = rejectorProfile,
                reason = reason,
            )
        )
    }

    fun createReviewReceived(
        receiver: User,
        receiverProfile: UserSportProfile,
        reviewId: String,
        reviewerProfile: UserSportProfile,
    ): Notification {
        return notificationRepository.save(
            Notification.createReviewReceived(
                receiver = receiver,
                receiverProfile = receiverProfile,
                reviewId = reviewId,
                reviewerProfile = reviewerProfile,
            )
        )
    }

    @Transactional
    fun markAsRead(
        userId: String,
        notificationId: String,
    ) {
        val notification = notificationRepository.findByIdFetchUser(notificationId)
            ?: throw CustomException(ErrorCode.NOTIFICATION_NOT_FOUND)

        // 본인 알림인지 검증
        if (notification.receiverUser.id != userId) {
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
        // 최초 요청 시 snapshotAt이 없으면 현재 시각으로 고정
        val snapshotAt = request.snapshotAt ?: TimeUtils.nowOffsetDateTime()

        // 알림 목록 cursor 기반 페이징 조회
        // - 로그인 유저가 수신자인 알림만 조회
        val page = notificationRepository.fetchMyNotificationPage(
            userId = userId,
            request = request,
            snapshotAt = snapshotAt,
        )

        return CursorResponse(
            snapshotAt = page.snapshotAt,
            results = NotificationSummaryResponse.from(page.results),
            nextCursor = page.nextCursor,
            hasNext = page.hasNext,
        )
    }
}
