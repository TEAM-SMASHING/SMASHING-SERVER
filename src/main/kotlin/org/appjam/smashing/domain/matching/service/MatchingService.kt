package org.appjam.smashing.domain.matching.service

import org.appjam.smashing.domain.game.entity.Game
import org.appjam.smashing.domain.game.repository.GameRepository
import org.appjam.smashing.domain.matching.dto.response.ReceivedMatchingSummaryResponse
import org.appjam.smashing.domain.matching.dto.response.SentMatchingSummaryResponse
import org.appjam.smashing.domain.matching.entity.Matching
import org.appjam.smashing.domain.matching.enums.MatchingStatus
import org.appjam.smashing.domain.matching.repository.MatchingRepository
import org.appjam.smashing.domain.notification.enums.NotificationType
import org.appjam.smashing.domain.notification.service.NotificationService
import org.appjam.smashing.domain.outbox.components.OutboxEventPublisher
import org.appjam.smashing.domain.outbox.dto.MatchingAcceptNotificationCreatedPayload
import org.appjam.smashing.domain.outbox.dto.MatchingReceivedPayload
import org.appjam.smashing.domain.outbox.dto.MatchingRequestNotificationCreatedPayload
import org.appjam.smashing.domain.outbox.dto.MatchingUpdatedPayload
import org.appjam.smashing.domain.outbox.enums.MatchingUpdateStatus
import org.appjam.smashing.domain.outbox.enums.SseEventType
import org.appjam.smashing.domain.review.repository.GameReviewRepository
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.appjam.smashing.domain.user.repository.UserRepository
import org.appjam.smashing.domain.user.repository.UserSportProfileRepository
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorResponse
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.appjam.smashing.global.util.TimeUtils
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
    private val gameRepository: GameRepository,
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

        // 하루 (00:00 ~) 최대 3회 게임 가능
        /* TODO: 앱잼 기간내 하루 3회 제한 해제
        validateDailyLimit(
            requesterUserId = requesterUserId,
            receiverUserId = receiverProfile.user.id!!,
        )
        */

        // 24시간 내 진행되지 않은 매칭 요청 이력이 남아있는 경우, 동일 상대방에 대해 중복 매칭 요청 불가
        validateNoMatchingRequestWithin24h(
            requesterUserId = requesterUserId,
            receiverUserId = receiverProfile.user.id!!,
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
        val savedNotification = notificationService.createMatchingRequested(
            receiver = receiverProfile.user,
            receiverProfile = receiverProfile,
            requesterProfile = requesterProfile,
        )

        val notificationCreatedAt = savedNotification.createdAt
            .atZone(DEFAULT_ZONE_ID)
            .toOffsetDateTime()
            .toString()

        // SSE 이벤트 발행
        publishMatchingReceived(
            receiverUserId = receiverProfile.user.id!!,
            matchingId = matchingId,
            sportId = sportId,
            receiverProfileId = receiverProfileId,
            requesterProfile = requesterProfile,
            reviewCount = reviewCount,
        )

        // 알림 생성 이벤트 발행
        publishMatchingRequestNotificationCreated(
            receiverUserId = receiverProfile.user.id!!,
            notificationId = savedNotification.id!!,
            notificationCreatedAt = notificationCreatedAt,
            matchingId = matchingId,
            sportId = sportId,
            receiverProfileId = receiverProfileId,
            requesterProfile = requesterProfile,
        )
    }

    @Transactional
    fun acceptMatching(
        receiverUserId: String,
        matchingId: String,
    ) {
        val matching = matchingRepository.findByIdFetchAllForUpdate(matchingId)
            ?: throw CustomException(ErrorCode.MATCHING_NOT_FOUND)

        // 수락 가능 여부 검증
        validateAcceptable(matching, receiverUserId)

        // 매칭 수락 처리
        matching.accept(LocalDateTime.now(DEFAULT_ZONE_ID)) // TODO: 인증 붙으면 receiver 타임존으로 교체

        // 다른 매칭 요청들 soft delete 처리
        matchingRepository.softDeleteRequestedBetweenUsersExcept(
            deletedAt = LocalDateTime.now(DEFAULT_ZONE_ID),
            status = MatchingStatus.REQUESTED,
            excludeMatchingId = matchingId,
            userA = matching.requester.id!!,
            userB = matching.receiver.id!!,
        )

        // 게임 엔티티 생성 (중복 방지)
        if (!gameRepository.existsByMatchingId(matchingId)) {
            gameRepository.save(Game.createFromMatching(matching))
        }

        val receiverProfile = findUserProfileBySport(receiverUserId, matching.sport.id!!)

        val requesterProfile = findUserProfileBySport(
            userId = matching.requester.id!!,
            sportId = matching.sport.id!!,
        )

        // 알림 생성
        val savedNotification = notificationService.createMatchingAccepted(
            receiver = matching.requester,
            receiverProfile = requesterProfile,
            acceptorProfile = receiverProfile,
        )

        // 알림 생성 시간 OffsetDateTime으로 변환
        val notificationCreatedAt = savedNotification.createdAt
            .atZone(DEFAULT_ZONE_ID)
            .toOffsetDateTime()
            .toString()

        // SSE 이벤트 발행
        publishMatchingUpdatedAccepted(
            requesterUserId = matching.requester.id!!,
            matchingId = matchingId,
        )

        // 알림 생성 이벤트 발행
        publishMatchingAcceptNotificationCreated(
            requesterUserId = matching.requester.id!!,
            notificationId = savedNotification.id!!,
            notificationCreatedAt = notificationCreatedAt,
            matchingId = matchingId,
            sportId = matching.sport.id!!,
            receiverProfileId = receiverProfile.id!!,
            receiverUserId = receiverUserId,
            receiverProfile = receiverProfile,
        )
    }

    @Transactional
    fun rejectMatching(
        receiverUserId: String,
        matchingId: String,
    ) {
        val matching = matchingRepository.findByIdFetchAllForUpdate(matchingId)
            ?: throw CustomException(ErrorCode.MATCHING_NOT_FOUND)

        validateRejectable(matching, receiverUserId)

        // 상태 변경
        matching.reject(LocalDateTime.now(DEFAULT_ZONE_ID))
        matchingRepository.flush()

        // soft delete
        matchingRepository.delete(matching)

        // SSE 이벤트 발행
        publishMatchingUpdatedRejected(
            requesterUserId = matching.requester.id!!,
            matchingId = matchingId,
        )
    }

    @Transactional
    fun cancelMyMatchingRequest(
        requesterUserId: String,
        matchingId: String,
    ) {
        val matching = matchingRepository.findByIdFetchAllForUpdate(matchingId)
            ?: throw CustomException(ErrorCode.MATCHING_NOT_FOUND)

        validateCancellableByRequester(matching, requesterUserId)

        // 상태 변경
        matching.cancel(LocalDateTime.now(DEFAULT_ZONE_ID))
        matchingRepository.flush()

        // soft delete
        matchingRepository.delete(matching)

        // SSE 이벤트 발행
        publishMatchingUpdatedCancelled(
            receiverUserId = matching.receiver.id!!,
            matchingId = matchingId,
        )
    }

    @Transactional(readOnly = true)
    fun getReceivedMatchings(
        userId: String,
        request: CommonCursorRequest,
    ): CursorResponse<ReceivedMatchingSummaryResponse> {

        val user = userRepository.findByIdOrNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val activeProfileId = user.activeUserSportProfileId
            ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)

        val activeProfile = userSportProfileRepository.findByIdOrNull(activeProfileId)
            ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)

        val sportId = activeProfile.sport.id
            ?: throw CustomException(ErrorCode.SPORT_NOT_FOUND)

        val snapshotAt = request.snapshotAt ?: TimeUtils.nowOffsetDateTime()

        val response = matchingRepository.fetchReceivedRequestedPage(
            receiverUserId = userId,
            sportId = sportId,
            request = request,
            snapshotAt = snapshotAt,
        )

        return CursorResponse(
            snapshotAt = response.snapshotAt,
            results = ReceivedMatchingSummaryResponse.from(response.results),
            nextCursor = response.nextCursor,
            hasNext = response.hasNext,
        )
    }

    @Transactional(readOnly = true)
    fun getSentMatchings(
        userId: String,
        request: CommonCursorRequest,
    ): CursorResponse<SentMatchingSummaryResponse> {

        val user = userRepository.findByIdOrNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val activeProfileId = user.activeUserSportProfileId
            ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)

        val activeProfile = userSportProfileRepository.findByIdOrNull(activeProfileId)
            ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)

        val sportId = activeProfile.sport.id
            ?: throw CustomException(ErrorCode.SPORT_NOT_FOUND)

        val snapshotAt = request.snapshotAt ?: TimeUtils.nowOffsetDateTime()

        val response = matchingRepository.fetchSentRequestedPage(
            requesterUserId = userId,
            sportId = sportId,
            request = request,
            snapshotAt = snapshotAt,
        )

        return CursorResponse(
            snapshotAt = response.snapshotAt,
            results = SentMatchingSummaryResponse.from(response.results),
            nextCursor = response.nextCursor,
            hasNext = response.hasNext,
        )
    }

    private fun findReceiverProfile(
        receiverProfileId: String
    ) = userSportProfileRepository.findByIdFetchAll(receiverProfileId)
        ?: throw CustomException(ErrorCode.MATCHING_RECEIVER_PROFILE_NOT_FOUND)

    private fun findRequesterUser(
        requesterUserId: String
    ) = userRepository.findByIdOrNull(requesterUserId)
        ?: throw CustomException(ErrorCode.MATCHING_REQUESTER_NOT_FOUND)

    private fun findRequesterProfileBySport(
        requesterUserId: String,
        sportId: Long
    ) = userSportProfileRepository.findByUserIdAndSportIdFetch(requesterUserId, sportId)
        ?: throw CustomException(ErrorCode.MATCHING_REQUESTER_NOT_FOUND)

    private fun validateNotSelf(
        requesterUserId: String,
        receiverUserId: String
    ) {
        if (requesterUserId == receiverUserId) {
            throw CustomException(ErrorCode.MATCHING_CANNOT_REQUEST_TO_SELF)
        }
    }

    private fun validateDailyLimit(
        requesterUserId: String,
        receiverUserId: String
    ) {
        val now = LocalDateTime.now(DEFAULT_ZONE_ID)
        val startOfDay = now.toLocalDate().atStartOfDay()

        // 하루 확정 게임 갯수 조회
        val todayConfirmedGames = gameRepository.countTodayConfirmedGamesBetweenUsers(
            startAt = startOfDay,
            userA = requesterUserId,
            userB = receiverUserId,
        )

        // 하루 최대 3회 제한
        if (todayConfirmedGames >= 3L) {
            throw CustomException(ErrorCode.MATCHING_DAILY_LIMIT_EXCEEDED)
        }
    }

    private fun validateRejectable(
        matching: Matching,
        receiverUserId: String,
    ) {
        // receiver만 거절 가능
        if (matching.receiver.id!! != receiverUserId) {
            throw CustomException(ErrorCode.MATCHING_FORBIDDEN)
        }

        // REQUESTED 상태만 거절 가능
        if (matching.status != MatchingStatus.REQUESTED) {
            throw CustomException(ErrorCode.MATCHING_ALREADY_RESPONDED)
        }
    }

    private fun validateCancellableByRequester(
        matching: Matching,
        requesterUserId: String,
    ) {
        // requester만 삭제 가능
        if (matching.requester.id!! != requesterUserId) {
            throw CustomException(ErrorCode.MATCHING_FORBIDDEN)
        }

        // REQUESTED 상태만 삭제 가능
        if (matching.status != MatchingStatus.REQUESTED) {
            throw CustomException(ErrorCode.MATCHING_ALREADY_RESPONDED)
        }
    }

    private fun validateNoMatchingRequestWithin24h(
        requesterUserId: String,
        receiverUserId: String,
    ) {
        val now = LocalDateTime.now(DEFAULT_ZONE_ID)
        val since = now.minusHours(24)

        val existsBlockedHistory = matchingRepository.existsBetweenUsersSinceExcludingAcceptedAndCompletedRaw(since, requesterUserId, receiverUserId) == 1L

        if (existsBlockedHistory) {
            throw CustomException(ErrorCode.MATCHING_PENDING_EXISTS)
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
        sportId: Long,
        receiverProfileId: String,
        requesterProfile: UserSportProfile,
        reviewCount: Long,
    ) {
        outboxEventPublisher.publish(
            userId = receiverUserId,
            eventType = SseEventType.MATCHING_RECEIVED,
            payload = MatchingReceivedPayload(
                matchingId = matchingId,
                sportId = sportId,
                receiverProfileId = receiverProfileId,
                requester = MatchingReceivedPayload.MatchingRequesterSummary(
                    userId = requesterProfile.user.id!!,
                    nickname = requesterProfile.user.nickname,
                    gender = requesterProfile.user.gender,
                    tierCode = requesterProfile.tier.code,
                    wins = requesterProfile.wins,
                    losses = requesterProfile.losses,
                    reviewCount = reviewCount,
                )
            )
        )
    }

    private fun publishMatchingUpdatedAccepted(
        requesterUserId: String,
        matchingId: String,
    ) {
        outboxEventPublisher.publish(
            userId = requesterUserId,
            eventType = SseEventType.MATCHING_UPDATED,
            payload = MatchingUpdatedPayload(
                matchingId = matchingId,
                status = MatchingUpdateStatus.ACCEPTED,
            )
        )
    }

    private fun publishMatchingRequestNotificationCreated(
        receiverUserId: String,
        notificationId: String,
        notificationCreatedAt: String,
        matchingId: String,
        sportId: Long,
        receiverProfileId: String,
        requesterProfile: UserSportProfile,
    ) {
        outboxEventPublisher.publish(
            userId = receiverUserId,
            eventType = SseEventType.MATCHING_REQUEST_NOTIFICATION_CREATED,
            payload = MatchingRequestNotificationCreatedPayload(
                notificationId = notificationId,
                notificationType = NotificationType.MATCHING_REQUESTED,
                notificationCreatedAt = notificationCreatedAt,
                matchingId = matchingId,
                sportId = sportId,
                receiverProfileId = receiverProfileId,
                requester = MatchingRequestNotificationCreatedPayload.RequesterSummary(
                    userId = requesterProfile.user.id!!,
                    nickname = requesterProfile.user.nickname,
                    tierCode = requesterProfile.tier.code,
                )
            )
        )
    }

    private fun publishMatchingUpdatedRejected(
        requesterUserId: String,
        matchingId: String,
    ) {
        outboxEventPublisher.publish(
            userId = requesterUserId,
            eventType = SseEventType.MATCHING_UPDATED,
            payload = MatchingUpdatedPayload(
                matchingId = matchingId,
                status = MatchingUpdateStatus.REJECTED,
            )
        )
    }

    private fun publishMatchingUpdatedCancelled(
        receiverUserId: String,
        matchingId: String,
    ) {
        outboxEventPublisher.publish(
            userId = receiverUserId,
            eventType = SseEventType.MATCHING_UPDATED,
            payload = MatchingUpdatedPayload(
                matchingId = matchingId,
                status = MatchingUpdateStatus.CANCELLED,
            )
        )
    }

    private fun validateAcceptable(
        matching: Matching,
        receiverUserId: String
    ) {
        // receiver만 수락 가능
        if (matching.receiver.id!! != receiverUserId) {
            throw CustomException(ErrorCode.MATCHING_FORBIDDEN)
        }

        // REQUESTED 상태만 수락 가능
        if (matching.status != MatchingStatus.REQUESTED) {
            throw CustomException(ErrorCode.MATCHING_ALREADY_RESPONDED)
        }
    }

    private fun findUserProfileBySport(
        userId: String,
        sportId: Long
    ): UserSportProfile = userSportProfileRepository.findByUserIdAndSportIdFetch(userId, sportId)
        ?: throw CustomException(ErrorCode.MATCHING_RECEIVER_USER_NOT_FOUND)

    private fun publishMatchingAcceptNotificationCreated(
        requesterUserId: String,
        notificationId: String,
        notificationCreatedAt: String,
        matchingId: String,
        sportId: Long,
        receiverProfileId: String,
        receiverUserId: String,
        receiverProfile: UserSportProfile,
    ) {
        outboxEventPublisher.publish(
            userId = requesterUserId,
            eventType = SseEventType.MATCHING_ACCEPT_NOTIFICATION_CREATED,
            payload = MatchingAcceptNotificationCreatedPayload(
                notificationId = notificationId,
                notificationType = NotificationType.MATCHING_ACCEPTED,
                notificationCreatedAt = notificationCreatedAt,
                matchingId = matchingId,
                sportId = sportId,
                receiverProfileId = receiverProfileId,
                acceptor = MatchingAcceptNotificationCreatedPayload.AcceptorSummary(
                    userId = receiverUserId,
                    nickname = receiverProfile.user.nickname,
                    tierCode = receiverProfile.tier.code,
                )
            )
        )
    }

    companion object {
        val DEFAULT_ZONE_ID = ZoneId.of("Asia/Seoul")
    }
}
