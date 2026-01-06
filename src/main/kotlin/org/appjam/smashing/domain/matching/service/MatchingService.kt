package org.appjam.smashing.domain.matching.service

import org.appjam.smashing.domain.matching.entity.Matching
import org.appjam.smashing.domain.matching.repository.MatchingRepository
import org.appjam.smashing.domain.notification.enums.NotificationType
import org.appjam.smashing.domain.notification.service.NotificationService
import org.appjam.smashing.domain.outbox.components.OutboxEventPublisher
import org.appjam.smashing.domain.outbox.dto.MatchingReceivedPayload
import org.appjam.smashing.domain.outbox.dto.NotificationCreatedPayload
import org.appjam.smashing.domain.outbox.enums.SseEventType
import org.appjam.smashing.domain.review.repository.GameReviewRepository
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.appjam.smashing.domain.user.repository.UserRepository
import org.appjam.smashing.domain.user.repository.UserSportProfileRepository
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class MatchingService(
    private val matchingRepository: MatchingRepository,
    private val userRepository: UserRepository,
    private val userSportProfileRepository: UserSportProfileRepository,
    private val gameReviewRepository: GameReviewRepository,
    private val notificationService: NotificationService,
    private val outboxEventPublisher: OutboxEventPublisher,
) {

    @Transactional
    fun requestMatching(
        requesterUserId: String,
        receiverProfileId: String,
    ) {
        val receiverProfile = findReceiverProfile(receiverProfileId)

        // 자기 자신에게 매칭 요청 불가
        validateNotSelf(requesterUserId, receiverProfile.user.id!!)

        val requesterUser = findRequesterUser(requesterUserId)

        val sportId = receiverProfile.sport.id ?: throw CustomException(ErrorCode.BAD_REQUEST)
        val requesterProfile = findRequesterProfileBySport(requesterUserId, sportId)

        // 하루 (00:00 ~) 최대 3회 매칭 요청 가능
        validateDailyLimit(
            requesterUserId,
            receiverProfile.user.id!!
        )

        // 매칭 생성
        val matchingId = createMatching(
            requesterUser = requesterUser,
            receiverUser = receiverProfile.user,
            receiverProfile = receiverProfile,
        )

        // 신청자 해당 스포츠 리뷰 개수 조회
        val reviewCount = countRequesterReviewsBySport(
            requesterUserId = requesterUserId,
            sportId = sportId,
        )

        // 알림 생성
        val notificationId = notificationService.createMatchingRequested(
            receiver = receiverProfile.user,
            requesterProfile = requesterProfile,
        )

        // SSE 이벤트 발행
        publishMatchingReceived(
            receiverUserId = receiverProfile.user.id!!,
            matchingId = matchingId,
            requesterProfile = requesterProfile,
            reviewCount = reviewCount,
        )

        // 알림 생성 이벤트 발행
        publishNotificationCreated(
            receiverUserId = receiverProfile.user.id!!,
            notificationId = notificationId,
            matchingId = matchingId,
        )
    }

    private fun findReceiverProfile(receiverProfileId: String): UserSportProfile = userSportProfileRepository.findByIdFetchAll(receiverProfileId)
        ?: throw CustomException(ErrorCode.MATCHING_RECEIVER_PROFILE_NOT_FOUND)

    private fun findRequesterUser(requesterUserId: String): User = userRepository.findByIdOrNull(requesterUserId)
            ?: throw CustomException(ErrorCode.MATCHING_REQUESTER_NOT_FOUND)

    private fun findRequesterProfileBySport(requesterUserId: String, sportId: Long): UserSportProfile = userSportProfileRepository.findByUserIdAndSportIdFetch(requesterUserId, sportId)
            ?: throw CustomException(ErrorCode.MATCHING_REQUESTER_NOT_FOUND)

    private fun validateNotSelf(requesterUserId: String, receiverUserId: String) {
        if (requesterUserId == receiverUserId) {
            throw CustomException(ErrorCode.MATCHING_CANNOT_REQUEST_TO_SELF)
        }
    }

    private fun validateDailyLimit(requesterUserId: String, receiverUserId: String) {
        // val zoneId = SecurityUtils.currentZoneId() TODO: 인증 붙이고 해제
        val zoneId = ZoneId.of("Asia/Seoul")

        val now = LocalDateTime.now(zoneId)
        val startOfDay = now.toLocalDate().atStartOfDay()

        val todayCount = matchingRepository.countTodayBetweenUsersIncludingDeleted(
            userA = requesterUserId,
            userB = receiverUserId,
            startAt = startOfDay,
        )

        if (todayCount >= 3L) {
            throw CustomException(ErrorCode.MATCHING_DAILY_LIMIT_EXCEEDED)
        }
    }

    private fun createMatching(
        requesterUser: User,
        receiverUser: User,
        receiverProfile: UserSportProfile,
    ): String {
        val matching = matchingRepository.save(
            Matching.createRequested(
                requester = requesterUser,
                receiver = receiverUser,
                sport = receiverProfile.sport,
            )
        )
        return requireNotNull(matching.id)
    }

    private fun countRequesterReviewsBySport(
        requesterUserId: String,
        sportId: Long,
    ): Long = gameReviewRepository.countByRevieweeAndSport(
            revieweeUserId = requesterUserId,
            sportId = sportId,
        )

    private fun publishMatchingReceived(
        receiverUserId: String,
        matchingId: String,
        requesterProfile: UserSportProfile,
        reviewCount: Long,
    ) {
        outboxEventPublisher.publish(
            userId = receiverUserId,
            eventType = SseEventType.MATCHING_RECEIVED,
            payload = MatchingReceivedPayload(
                matchingId = matchingId,
                requester = MatchingReceivedPayload.MatchingRequesterSummary(
                    userId = requesterProfile.user.id!!,
                    nickname = requesterProfile.user.nickname,
                    gender = requesterProfile.user.gender,
                    tierName = requesterProfile.tier.name,
                    wins = requesterProfile.wins,
                    losses = requesterProfile.losses,
                    reviewCount = reviewCount,
                )
            )
        )
    }

    private fun publishNotificationCreated(
        receiverUserId: String,
        notificationId: String,
        matchingId: String,
    ) {
        outboxEventPublisher.publish(
            userId = receiverUserId,
            eventType = SseEventType.NOTIFICATION_CREATED,
            payload = NotificationCreatedPayload(
                notificationId = notificationId,
                notificationType = NotificationType.MATCHING_REQUESTED,
                targetId = matchingId,
            )
        )
    }
}
