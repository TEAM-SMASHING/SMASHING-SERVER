package org.appjam.smashing.domain.matching.service

import org.appjam.smashing.domain.game.entity.Game
import org.appjam.smashing.domain.game.repository.GameRepository
import org.appjam.smashing.domain.matching.dto.response.ReceivedMatchingSummaryResponse
import org.appjam.smashing.domain.matching.dto.response.SentMatchingSummaryResponse
import org.appjam.smashing.domain.matching.entity.Matching
import org.appjam.smashing.domain.matching.enums.MatchingStatus
import org.appjam.smashing.domain.matching.repository.MatchingRepository
import org.appjam.smashing.domain.notification.service.NotificationService
import org.appjam.smashing.domain.outbox.components.OutboxEventPublisher
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

        // TODO: 차단 관련 정책 확정 후 삽입
        // TODO: 26.03.10 기준 기능 명세 확정 x -> 확정 후 다시 검토 필요

        // sport receiverProfile의 sport로 결정
        val sport = receiverProfile.sport
        val sportId = sport.id ?: throw CustomException(ErrorCode.SPORT_NOT_FOUND)

        // requesterProfile: 동일 sport 기준
        val requesterProfile = userSportProfileRepository.findByUserIdAndSportCode(
            userId = requesterUserId,
            sportCode = sport.code,
        ) ?: throw CustomException(ErrorCode.ACTIVE_PROFILE_NOT_FOUND)

        val requesterProfileIdNotNull = requesterProfile.id ?: throw CustomException(ErrorCode.ACTIVE_PROFILE_NOT_FOUND)
        val receiverProfileIdNotNull = receiverProfile.id ?: throw CustomException(ErrorCode.MATCHING_RECEIVER_PROFILE_NOT_FOUND)

        // 하루 3판 제한 (RESULT_CONFIRMED 게임 기준)
        validateDailyLimit(
            profileA = requesterProfileIdNotNull,
            profileB = receiverProfileIdNotNull,
            sportId = sportId,
        )

        // 24h 쿨다운 (요청/취소/거절 기준)
        validateCooldown(
            profileA = requesterProfileIdNotNull,
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
                    requesterProfileId = requesterProfileIdNotNull,
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
    fun cancelMyMatchingRequest(
        requesterUserId: String,
        matchingId: String,
    ) {
        val matching = matchingRepository.findByIdFetchAllForUpdate(matchingId)
            ?: throw CustomException(ErrorCode.MATCHING_NOT_FOUND)

        validateCancellableByRequester(matching, requesterUserId)

        // 취소 처리
        matching.cancel(LocalDateTime.now(TimeUtils.DEFAULT_ZONE_ID))
        matchingRepository.flush()

        // soft delete
        matchingRepository.delete(matching)

        // SSE - receiver 받은 매칭 탭에서 카드 제거
        outboxEventPublisher.publish(
            userId = matching.receiverProfile.user.id!!,
            eventType = SseEventType.MATCHING_UPDATED,
            payload = MatchingUpdatedPayload(
                matchingId = matchingId,
                status = MatchingUpdateStatus.CANCELLED,
            )
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

        // 거절 처리
        matching.reject(LocalDateTime.now(TimeUtils.DEFAULT_ZONE_ID))
        matchingRepository.flush()

        // soft delete
        matchingRepository.delete(matching)

        // SSE - receiver 받은 매칭 탭에서 카드 제거
        outboxEventPublisher.publish(
            userId = receiverUserId,
            eventType = SseEventType.MATCHING_UPDATED,
            payload = MatchingUpdatedPayload(
                matchingId = matchingId,
                status = MatchingUpdateStatus.REJECTED,
            )
        )

        // SSE - requester 보낸 매칭 탭에서 카드 제거
        outboxEventPublisher.publish(
            userId = matching.requesterProfile.user.id!!,
            eventType = SseEventType.MATCHING_UPDATED,
            payload = MatchingUpdatedPayload(
                matchingId = matchingId,
                status = MatchingUpdateStatus.REJECTED,
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

        validateAcceptable(matching, receiverUserId)

        val now = LocalDateTime.now(TimeUtils.DEFAULT_ZONE_ID)

        // 매칭 수락 처리
        matching.accept(now)

        // 게임 엔티티 생성 (중복 방지)
        if (!gameRepository.existsByMatchingId(matchingId)) {
            gameRepository.save(Game.createFromMatching(matching))
        }

        val receiverProfile = findUserProfileBySport(
            userId = receiverUserId,
            sportId = matching.sport.id!!,
        )

        val requesterProfile = findUserProfileBySport(
            userId = matching.requesterProfile.user.id!!,
            sportId = matching.sport.id!!,
        )

        // 상대방(requester) 알림 저장
        notificationService.createMatchingAccepted(
            receiver = matching.requesterProfile.user,
            receiverProfile = requesterProfile,
            acceptorProfile = receiverProfile,
        )

        // SSE - receiver 받은 매칭 탭에서 카드 제거
        outboxEventPublisher.publish(
            userId = matching.requesterProfile.user.id!!,
            eventType = SseEventType.MATCHING_UPDATED,
            payload = MatchingUpdatedPayload(
                matchingId = matchingId,
                status = MatchingUpdateStatus.ACCEPTED,
            )
        )
    }

//    @Transactional(readOnly = true)
//    fun getReceivedMatchings(
//        userId: String,
//        request: CommonCursorRequest,
//    ): CursorResponse<ReceivedMatchingSummaryResponse> {
//
//        val user = userRepository.findByIdOrNull(userId)
//            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
//
//        val activeProfileId = user.activeUserSportProfileId
//            ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)
//
//        val activeProfile = userSportProfileRepository.findByIdOrNull(activeProfileId)
//            ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)
//
//        val sportId = activeProfile.sport.id
//            ?: throw CustomException(ErrorCode.SPORT_NOT_FOUND)
//
//        val snapshotAt = request.snapshotAt ?: TimeUtils.nowOffsetDateTime()
//
//        val response = matchingRepository.fetchReceivedRequestedPage(
//            receiverUserId = userId,
//            sportId = sportId,
//            request = request,
//            snapshotAt = snapshotAt,
//        )
//
//        return CursorResponse(
//            snapshotAt = response.snapshotAt,
//            results = ReceivedMatchingSummaryResponse.from(response.results),
//            nextCursor = response.nextCursor,
//            hasNext = response.hasNext,
//        )
//    }

//    @Transactional(readOnly = true)
//    fun getSentMatchings(
//        userId: String,
//        request: CommonCursorRequest,
//    ): CursorResponse<SentMatchingSummaryResponse> {
//
//        val user = userRepository.findByIdOrNull(userId)
//            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
//
//        val activeProfileId = user.activeUserSportProfileId
//            ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)
//
//        val activeProfile = userSportProfileRepository.findByIdOrNull(activeProfileId)
//            ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)
//
//        val sportId = activeProfile.sport.id
//            ?: throw CustomException(ErrorCode.SPORT_NOT_FOUND)
//
//        val snapshotAt = request.snapshotAt ?: TimeUtils.nowOffsetDateTime()
//
//        val response = matchingRepository.fetchSentRequestedPage(
//            requesterUserId = userId,
//            sportId = sportId,
//            request = request,
//            snapshotAt = snapshotAt,
//        )
//
//        return CursorResponse(
//            snapshotAt = response.snapshotAt,
//            results = SentMatchingSummaryResponse.from(response.results),
//            nextCursor = response.nextCursor,
//            hasNext = response.hasNext,
//        )
//    }

    private fun validateDailyLimit(
        profileA: String,
        profileB: String,
        sportId: Long,
    ) {
        val today = LocalDate.now(TimeUtils.DEFAULT_ZONE_ID)
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

    private fun validateCancellableByRequester(
        matching: Matching,
        requesterUserId: String,
    ) {
        // requester만 취소 가능
        if (matching.requesterProfile.user.id!! != requesterUserId) {
            throw CustomException(ErrorCode.MATCHING_FORBIDDEN)
        }

        // REQUESTED 상태만 취소 가능
        if (matching.status != MatchingStatus.REQUESTED) {
            throw CustomException(ErrorCode.MATCHING_ALREADY_RESPONDED)
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

    private fun validateAcceptable(
        matching: Matching,
        receiverUserId: String,
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
        sportId: Long,
    ): UserSportProfile = userSportProfileRepository.findByUserIdAndSportIdFetch(userId, sportId)
            ?: throw CustomException(ErrorCode.MATCHING_RECEIVER_USER_NOT_FOUND)
}
