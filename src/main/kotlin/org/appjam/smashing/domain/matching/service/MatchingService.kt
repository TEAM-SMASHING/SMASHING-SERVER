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
import org.appjam.smashing.domain.outbox.dto.MatchingSentPayload
import org.appjam.smashing.domain.outbox.dto.MatchingUpdatedPayload
import org.appjam.smashing.domain.outbox.enums.MatchingUpdateStatus
import org.appjam.smashing.domain.outbox.enums.SseEventType
import org.appjam.smashing.domain.review.repository.GameReviewRepository
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
import java.time.LocalDate
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
        val now = TimeUtils.nowOffsetDateTime().toLocalDateTime()

        val requesterUser = userRepository.findByIdOrNull(requesterUserId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val receiverProfile = userSportProfileRepository.findByIdOrNull(receiverProfileId)
            ?: throw CustomException(ErrorCode.MATCHING_RECEIVER_PROFILE_NOT_FOUND)

        val receiverUser = receiverProfile.user

        // 자기 자신에게 매칭 신청 불가
        if (receiverUser.id == requesterUserId) {
            throw CustomException(ErrorCode.MATCHING_CANNOT_REQUEST_TO_SELF)
        }

        // sport receiverProfile의 sport로 결정
        val sport = receiverProfile.sport
        val sportId = sport.id ?: throw CustomException(ErrorCode.SPORT_NOT_FOUND)

        // requesterProfile: 동일 sport 기준
        val requesterProfile = userSportProfileRepository.findByUserIdAndSportCode(
            userId = requesterUserId,
            sportCode = sport.code,
        ) ?: throw CustomException(ErrorCode.ACTIVE_PROFILE_NOT_FOUND)

        val requesterProfileId = requesterProfile.id ?: throw CustomException(ErrorCode.ACTIVE_PROFILE_NOT_FOUND)
        val receiverProfileIdNotNull = receiverProfile.id ?: throw CustomException(ErrorCode.MATCHING_RECEIVER_PROFILE_NOT_FOUND)

        // 하루 3판 제한 (RESULT_CONFIRMED 게임 기준)
        validateDailyLimit(
            profileA = requesterProfileId,
            profileB = receiverProfileIdNotNull,
            sportId = sportId,
        )

        // 24h 쿨다운 (요청/취소/거절 기준)
        validateCooldown(
            profileA = requesterProfileId,
            profileB = receiverProfileIdNotNull,
            sportId = sportId,
            now = now,
        )

        // 매칭 생성
        val matching = matchingRepository.save(
            Matching.createRequested(
                requesterProfile = requesterProfile,
                receiverProfile = receiverProfile,
                sport = sport,
            )
        )

        // 알림 생성
        notificationService.createMatchingRequested(
            receiver = receiverUser,
            receiverProfile = receiverProfile,
            requesterProfile = requesterProfile,
        )

        /**
         * TODO: 프로필 기반 리뷰 카운트로 바꿀지 결정되면 교체 필요
         */
        val requesterReviewCount = gameReviewRepository.countByRevieweeAndSport(
            revieweeUserId = requesterUser.id!!,
            sportId = sportId,
        )
        val receiverReviewCount = gameReviewRepository.countByRevieweeAndSport(
            revieweeUserId = receiverUser.id!!,
            sportId = sportId,
        )

        // SSE - 상대방에게 받은 요청 실시간 반영 + 토스트
        outboxEventPublisher.publish(
            userId = receiverUser.id!!,
            eventType = SseEventType.MATCHING_RECEIVED,
            payload = MatchingReceivedPayload(
                matchingId = matching.id!!,
                sportCode = sport.code,
                receiverProfileId = receiverProfileIdNotNull,
                requester = MatchingReceivedPayload.MatchingRequesterSummary(
                    requesterProfileId = requesterProfileId,
                    nickname = requesterUser.nickname,
                    gender = requesterUser.gender,
                    tierCode = requesterProfile.tier.code,
                    wins = requesterProfile.wins,
                    losses = requesterProfile.losses,
                    reviewCount = requesterReviewCount,
                )
            )
        )

        // SSE - 나에게 보낸 요청 실시간 반영
        outboxEventPublisher.publish(
            userId = requesterUser.id!!,
            eventType = SseEventType.MATCHING_SENT,
            payload = MatchingSentPayload(
                matchingId = matching.id!!,
                sportCode = sport.code,
                receiverProfileId = receiverProfileIdNotNull,
                receiver = MatchingSentPayload.MatchingReceiverSummary(
                    receiverProfileId = receiverProfileIdNotNull,
                    nickname = receiverUser.nickname,
                    gender = receiverUser.gender,
                    tierCode = receiverProfile.tier.code,
                    wins = receiverProfile.wins,
                    losses = receiverProfile.losses,
                    reviewCount = receiverReviewCount,
                )
            )
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
            userA = matching.requesterProfile.user.id!!,
            userB = matching.receiverProfile.user.id!!,
            sportId = matching.sport.id!!,
        )

        // 게임 엔티티 생성 (중복 방지)
        if (!gameRepository.existsByMatchingId(matchingId)) {
            gameRepository.save(Game.createFromMatching(matching))
        }

        val receiverProfile = findUserProfileBySport(receiverUserId, matching.sport.id!!)

        val requesterProfile = findUserProfileBySport(
            userId = matching.requesterProfile.user.id!!,
            sportId = matching.sport.id!!,
        )

        // 알림 생성
        val savedNotification = notificationService.createMatchingAccepted(
            receiver = matching.requester!!,
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
            requesterUserId = matching.requesterProfile.user.id!!,
            matchingId = matchingId,
        )

        // 알림 생성 이벤트 발행
        publishMatchingAcceptNotificationCreated(
            requesterUserId = matching.requesterProfile.user.id!!,
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
            requesterUserId = matching.requesterProfile.user.id!!,
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
            receiverUserId = matching.receiverProfile.user.id!!,
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

    private fun validateDailyLimit(
        profileA: String,
        profileB: String,
        sportId: Long,
    ) {
        val zone = TimeUtils.DEFAULT_ZONE_ID
        val today = LocalDate.now(zone)
        val startOfDay = today.atStartOfDay()
        val endOfDay = today.plusDays(1).atStartOfDay()

        val confirmedCount = gameRepository.countConfirmedGamesTodayBetweenProfiles(
            profileA = profileA,
            profileB = profileB,
            sportId = sportId,
            startOfDay = startOfDay,
            endOfDay = endOfDay,
        )

        if (confirmedCount >= 3L) {
            throw CustomException(ErrorCode.MATCHING_DAILY_LIMIT_EXCEEDED)
        }
    }

    private fun validateCooldown(
        profileA: String,
        profileB: String,
        sportId: Long,
        now: LocalDateTime,
    ) {
        val latest = matchingRepository.findLatestForCooldown(
            profileA = profileA,
            profileB = profileB,
            sportId = sportId,
        ) ?: return

        val status = runCatching { MatchingStatus.valueOf(latest.status) }
            .getOrElse { return }

        when (status) {
            MatchingStatus.REQUESTED -> {
                val until = latest.createdAt.plusHours(24)
                if (now.isBefore(until)) throw CustomException(ErrorCode.MATCHING_PENDING_EXISTS)
            }

            MatchingStatus.CANCELLED,
            MatchingStatus.REJECTED -> {
                val base = latest.respondedAt ?: latest.createdAt
                val until = base.plusHours(24)
                if (now.isBefore(until)) throw CustomException(ErrorCode.MATCHING_PENDING_EXISTS)
            }

            else -> Unit
        }
    }

    private fun validateRejectable(
        matching: Matching,
        receiverUserId: String,
    ) {
        // receiver만 거절 가능
        if (matching.receiverProfile.user.id!! != receiverUserId) {
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
        if (matching.requesterProfile.user.id!! != requesterUserId) {
            throw CustomException(ErrorCode.MATCHING_FORBIDDEN)
        }

        // REQUESTED 상태만 삭제 가능
        if (matching.status != MatchingStatus.REQUESTED) {
            throw CustomException(ErrorCode.MATCHING_ALREADY_RESPONDED)
        }
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
        if (matching.receiverProfile.user.id!! != receiverUserId) {
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

    // 삭제 예정
    companion object { // TODO: DEFAULT 부분 constants로 통일 예정 모두 삭제
        val DEFAULT_ZONE_ID = ZoneId.of("Asia/Seoul")
    }
}
