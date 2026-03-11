package org.appjam.smashing.domain.game.service

import org.appjam.smashing.domain.game.dto.command.GameResultConfirmCommand
import org.appjam.smashing.domain.game.dto.command.GameResultRejectCommand
import org.appjam.smashing.domain.game.dto.command.GameResultSubmitCommand
import org.appjam.smashing.domain.game.dto.response.GameResultConfirmResponse
import org.appjam.smashing.domain.game.dto.response.GameResultSubmitLockResponse
import org.appjam.smashing.domain.game.dto.response.GameResultSubmitResponse
import org.appjam.smashing.domain.game.dto.response.PendingResultAcceptedGameSummaryResponse
import org.appjam.smashing.domain.game.entity.Game
import org.appjam.smashing.domain.game.entity.GameResultSubmission
import org.appjam.smashing.domain.game.enums.GameSubmissionRejectReason
import org.appjam.smashing.domain.game.enums.GameStatus
import org.appjam.smashing.domain.game.enums.GameSubmissionStatus
import org.appjam.smashing.domain.game.repository.GameRepository
import org.appjam.smashing.domain.game.repository.GameResultSubmissionRepository
import org.appjam.smashing.domain.lp.entity.LpHistory
import org.appjam.smashing.domain.lp.repository.LpHistoryRepository
import org.appjam.smashing.domain.matching.repository.MatchingRepository
import org.appjam.smashing.domain.notification.enums.NotificationType
import org.appjam.smashing.domain.notification.service.NotificationService
import org.appjam.smashing.domain.outbox.components.OutboxEventPublisher
import org.appjam.smashing.domain.outbox.dto.GameResultRejectedNotificationCreatedPayload
import org.appjam.smashing.domain.outbox.dto.GameUpdatedPayload
import org.appjam.smashing.domain.outbox.dto.ReviewReceivedNotificationCreatedPayload
import org.appjam.smashing.domain.outbox.enums.SseEventType
import org.appjam.smashing.domain.review.repository.GameReviewRepository
import org.appjam.smashing.domain.review.service.GameReviewService
import org.appjam.smashing.domain.tier.entity.Tier
import org.appjam.smashing.domain.tier.enums.TierCode
import org.appjam.smashing.domain.tier.repository.TierRepository
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
import java.time.temporal.ChronoUnit

@Service
class GameService(
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository,
    private val submissionRepository: GameResultSubmissionRepository,
    private val notificationService: NotificationService,
    private val outboxEventPublisher: OutboxEventPublisher,
    private val gameReviewService: GameReviewService,
    private val userSportProfileRepository: UserSportProfileRepository,
    private val lpHistoryRepository: LpHistoryRepository,
    private val tierRepository: TierRepository,
) {

    @Transactional
    fun submitResult(
        submitterUserId: String,
        gameId: String,
        command: GameResultSubmitCommand,
    ): GameResultSubmitResponse {
        val game = gameRepository.findByIdFetchAllForUpdate(gameId)
            ?: throw CustomException(ErrorCode.GAME_NOT_FOUND)

        val requesterProfile = game.matching.requesterProfile
        val receiverProfile = game.matching.receiverProfile

        // Host는 매칭을 수락한 사람(receiverProfile)
        if (receiverProfile.user.id != submitterUserId) {
            throw CustomException(ErrorCode.GAME_RESULT_SUBMIT_ALLOW_ONLY_HOST)
        }

        val now = LocalDateTime.now(TimeUtils.DEFAULT_ZONE_ID)
        val startOfDay = now.toLocalDate().atStartOfDay()

        // 이번 제출이 몇 번째 제출인지 계산
        val totalSubmissionCount = submissionRepository.countByGame_Id(gameId)
        val attemptNo = (totalSubmissionCount + 1).toInt()

        // 결과 제출은 최대 2회까지만 허용 (최초 제출 1회 + 재제출 1회)
        if (attemptNo > 2) {
            throw CustomException(ErrorCode.GAME_RESULT_SUBMISSION_LIMIT_EXCEEDED)
        }

        // 최초 제출 / 재제출 분기 검증
        when (attemptNo) {
            1 -> {
                // 최초 제출은 PENDING_RESULT 상태에서만 가능
                if (game.resultStatus != GameStatus.PENDING_RESULT) {
                    throw CustomException(ErrorCode.GAME_RESULT_ALREADY_SUBMITTED)
                }

                val todayConfirmedCount = gameRepository.countTodayConfirmedGamesBetweenProfiles(
                    startAt = startOfDay,
                    profileA = requesterProfile.id!!,
                    profileB = receiverProfile.id!!,
                )

                // 오늘 첫 경기면 생성 후 1시간 동안 결과 제출 불가
                if (todayConfirmedCount == 0L && ChronoUnit.MINUTES.between(game.createdAt, now) < 60) {
                    throw CustomException(ErrorCode.GAME_RESULT_SUBMIT_BLOCKED_1H)
                }

                // 오늘 2~3번째 경기면 10분 제한
                if (todayConfirmedCount in 1L..2L) {
                    val prevConfirmedAt = gameRepository.findTodayLatestConfirmedAtBetweenProfiles(
                        startAt = startOfDay,
                        profileA = requesterProfile.id!!,
                        profileB = receiverProfile.id!!,
                    )

                    if (prevConfirmedAt != null && ChronoUnit.MINUTES.between(prevConfirmedAt, now) <= 30 && ChronoUnit.MINUTES.between(game.createdAt, now) < 10) {
                        throw CustomException(ErrorCode.GAME_RESULT_SUBMIT_BLOCKED_10M)
                    }
                }

                // 최초 제출에서는 review 필수
                if (command.review == null) {
                    throw CustomException(ErrorCode.GAME_REVIEW_REQUIRED_ON_FIRST_SUBMISSION)
                }
            }

            2 -> {
                // 재제출은 경기가 RESULT_REJECTED 상태에서만 가능
                if (game.resultStatus != GameStatus.RESULT_REJECTED) {
                    throw CustomException(ErrorCode.GAME_RESULT_RESUBMIT_NOT_ALLOWED)
                }

                // 가장 최근 제출안 조회
                val latestSubmission = submissionRepository.findTopByGame_IdOrderByAttemptNoDesc(gameId)
                    ?: throw CustomException(ErrorCode.GAME_SUBMISSION_NOT_FOUND)

                // 재제출은 가장 최근 제출안이 REJECTED 상태일 때만 가능
                if (latestSubmission.status != GameSubmissionStatus.REJECTED) {
                    throw CustomException(ErrorCode.GAME_RESULT_RESUBMIT_NOT_ALLOWED)
                }

                // 재제출에서는 review 받지 않음
                if (command.review != null) {
                    throw CustomException(ErrorCode.GAME_REVIEW_ONLY_FIRST_SUBMISSION_ALLOWED)
                }
            }
        }

        // 승자/패자 프로필 검증
        val winnerProfile = when (command.winnerProfileId) {
            requesterProfile.id -> requesterProfile
            receiverProfile.id -> receiverProfile
            else -> throw CustomException(ErrorCode.GAME_RESULT_INVALID_PLAYERS)
        }

        val loserProfile = when (command.loserProfileId) {
            requesterProfile.id -> requesterProfile
            receiverProfile.id -> receiverProfile
            else -> throw CustomException(ErrorCode.GAME_RESULT_INVALID_PLAYERS)
        }

        if (winnerProfile.id == loserProfile.id) {
            throw CustomException(ErrorCode.GAME_RESULT_SAME_PLAYER)
        }

        // 게임 상태 변경
        game.markWaitingConfirmation()

        // 제출안 저장
        val submission = submissionRepository.save(
            GameResultSubmission.create(
                game = game,
                submitterProfile = receiverProfile,
                confirmerProfile = requesterProfile,
                winnerProfile = winnerProfile,
                loserProfile = loserProfile,
                attemptNo = attemptNo,
            )
        )

        // 최초 제출일 때만 리뷰 저장
        if (attemptNo == 1) {
            gameReviewService.createReview(
                game = game,
                reviewerProfile = receiverProfile,
                revieweeProfile = requesterProfile,
                rating = command.review!!.rating,
                content = command.review.content,
                tags = command.review.tags,
            )
        }

        // 상대방(requester) 알림 저장
        notificationService.createGameResultSubmitted(
            receiver = requesterProfile.user,
            receiverProfile = requesterProfile,
            submitterProfile = receiverProfile,
            game = game,
            submission = submission,
        )

        // 상대방(requester)에게 game.updated SSE 발행
        outboxEventPublisher.publish(
            userId = requesterProfile.user.id!!,
            eventType = SseEventType.GAME_UPDATED,
            payload = GameUpdatedPayload(
                gameId = game.id!!,
                submissionId = submission.id!!,
                submissionAttemptNo = submission.attemptNo,
                resultStatus = game.resultStatus,
            )
        )

        return GameResultSubmitResponse.from(submission.id!!)
    }

    @Transactional
    fun rejectResult(
        confirmerUserId: String,
        gameId: String,
        submissionId: String,
        command: GameResultRejectCommand,
    ) {
        val now = LocalDateTime.now(TimeUtils.DEFAULT_ZONE_ID)

        val game = gameRepository.findByIdForUpdate(gameId)
            ?: throw CustomException(ErrorCode.GAME_NOT_FOUND)

        // 현재 결과 확인 대기 상태인 경기만 반려 가능
        if (game.resultStatus != GameStatus.WAITING_CONFIRMATION) {
            throw CustomException(ErrorCode.GAME_RESULT_NOT_WAITING_CONFIRMATION)
        }

        val submission = submissionRepository.findByIdAndGameIdForUpdate(submissionId, gameId)
            ?: throw CustomException(ErrorCode.GAME_SUBMISSION_NOT_FOUND)

        // 아직 처리되지 않은 제출안만 반려 가능
        if (submission.status != GameSubmissionStatus.SUBMITTED) {
            throw CustomException(ErrorCode.GAME_SUBMISSION_NOT_SUBMITTED)
        }

        // 제출안의 confirmer(결과 확인자)만 반려 가능
        if (submission.confirmerProfile.user.id != confirmerUserId) {
            throw CustomException(ErrorCode.GAME_SUBMISSION_CONFIRMER_MISMATCH)
        }

        // 경기의 가장 최근 제출안일 경우에만 반려 가능
        val latestSubmission = submissionRepository.findTopByGame_IdOrderByAttemptNoDesc(gameId)
            ?: throw CustomException(ErrorCode.GAME_SUBMISSION_NOT_FOUND)

        if (latestSubmission.id != submission.id) {
            throw CustomException(ErrorCode.GAME_SUBMISSION_NOT_LATEST)
        }

        // 제출안 제출 회차에 따라 분기
        when (submission.attemptNo) {
            1 -> {
                // 1차 반려는 사유 필수
                val reason = command.reason
                    ?: throw CustomException(ErrorCode.GAME_RESULT_REJECT_REASON_REQUIRED)

                // 제출안 반려 처리
                submission.rejectWithReason(
                    reason = reason,
                    actedAt = now,
                )

                // game은 재제출 가능 상태로 변경
                game.markRejected()

                // Host에게 반려 알림 저장
                notificationService.createGameResultRejected(
                    receiver = submission.submitterProfile.user,
                    receiverProfile = submission.submitterProfile,
                    rejectorProfile = submission.confirmerProfile,
                    reason = reason,
                )

                // SSE - Host 화면 상태 실시간 반영
                outboxEventPublisher.publish(
                    userId = submission.submitterProfile.user.id!!,
                    eventType = SseEventType.GAME_UPDATED,
                    payload = GameUpdatedPayload(
                        gameId = game.id!!,
                        submissionId = submission.id!!,
                        submissionAttemptNo = submission.attemptNo,
                        resultStatus = game.resultStatus,
                    )
                )
            }

            2 -> {
                // 제출안 반려 처리
                submission.reject(
                    actedAt = now,
                )

                // game은 기록되지 않음 처리(취소)
                game.cancel()

                // SSE - Host 화면 상태 실시간 반영 (게임 취소)
                outboxEventPublisher.publish(
                    userId = submission.submitterProfile.user.id!!,
                    eventType = SseEventType.GAME_UPDATED,
                    payload = GameUpdatedPayload(
                        gameId = game.id!!,
                        submissionId = submission.id!!,
                        submissionAttemptNo = submission.attemptNo,
                        resultStatus = GameStatus.CANCELED,
                    )
                )
            }

            else -> {
                throw CustomException(ErrorCode.GAME_RESULT_REJECT_NOT_ALLOWED)
            }
        }
    }

    @Transactional
    fun confirmResult(
        confirmerUserId: String,
        gameId: String,
        submissionId: String,
        command: GameResultConfirmCommand,
    ): GameResultConfirmResponse {
        val now = LocalDateTime.now(TimeUtils.DEFAULT_ZONE_ID)

        val game = gameRepository.findByIdFetchAllForUpdate(gameId)
            ?: throw CustomException(ErrorCode.GAME_NOT_FOUND)

        // 1차 제출 승인 / 2차 재제출 승인 모두 WAITING_CONFIRMATION 상태에서만 가능
        if (game.resultStatus != GameStatus.WAITING_CONFIRMATION) {
            throw CustomException(ErrorCode.GAME_RESULT_NOT_WAITING_CONFIRMATION)
        }

        val submission = submissionRepository.findByIdAndGameIdForUpdate(submissionId, gameId)
            ?: throw CustomException(ErrorCode.GAME_SUBMISSION_NOT_FOUND)

        // 아직 처리되지 않은 제출안만 승인 가능
        if (submission.status != GameSubmissionStatus.SUBMITTED) {
            throw CustomException(ErrorCode.GAME_SUBMISSION_NOT_SUBMITTED)
        }

        // 결과 확인자(confirmer) 검증
        if (submission.confirmerProfile.user.id != confirmerUserId) {
            throw CustomException(ErrorCode.GAME_SUBMISSION_CONFIRMER_MISMATCH)
        }

        // 가장 최근 제출안만 승인 가능
        val latestSubmission = submissionRepository.findTopByGame_IdOrderByAttemptNoDesc(gameId)
            ?: throw CustomException(ErrorCode.GAME_SUBMISSION_NOT_FOUND)

        if (latestSubmission.id != submission.id) {
            throw CustomException(ErrorCode.GAME_SUBMISSION_NOT_LATEST)
        }

        // 제출안 승인 처리
        submission.accept(now)

        // 게임 최종 확정
        game.confirmResult(
            submissionId = submission.id!!,
            winnerProfile = submission.winnerProfile,
            loserProfile = submission.loserProfile,
            confirmedAt = now,
        )

        // 승/패 프로필을 락으로 다시 조회
        val sportId = game.sport.id!!

        val winnerProfile = userSportProfileRepository.findByUserIdAndSportIdForUpdate(
            submission.winnerProfile.user.id!!,
            sportId,
        ) ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)

        val loserProfile = userSportProfileRepository.findByUserIdAndSportIdForUpdate(
            submission.loserProfile.user.id!!,
            sportId,
        ) ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)

        // 고정 LP 정책 반영: 승자 +30 / 패자 -20(최소 0)
        applyLpAndTierUpdate(
            winnerProfile = winnerProfile,
            loserProfile = loserProfile,
            sportId = sportId,
            game = game,
        )

        // confirmer -> host 리뷰 저장
        val savedReview = gameReviewService.createReview(
            game = game,
            reviewerProfile = submission.confirmerProfile,
            revieweeProfile = submission.submitterProfile,
            rating = command.review.rating,
            content = command.review.content,
            tags = command.review.tags,
        )

        // Host에게 후기 도착 알림 저장
        notificationService.createReviewReceived(
            receiver = submission.submitterProfile.user,
            receiverProfile = submission.submitterProfile,
            reviewId = savedReview.id!!,
            reviewerProfile = submission.confirmerProfile,
        )

        // Host 화면에서 카드 제거되도록 game.updated SSE 발행
        outboxEventPublisher.publish(
            userId = submission.submitterProfile.user.id!!,
            eventType = SseEventType.GAME_UPDATED,
            payload = GameUpdatedPayload(
                gameId = game.id!!,
                submissionId = submission.id!!,
                submissionAttemptNo = submission.attemptNo,
                resultStatus = game.resultStatus,
            )
        )

        return GameResultConfirmResponse.from(savedReview.id!!)
    }

//    @Transactional(readOnly = true)
//    fun getSubmissionDetail(
//        gameId: String,
//        submissionId: String,
//    ): GameResultSubmissionDetailResponse {
//        val submission = submissionRepository.findDetailByIdAndGameId(
//            submissionId = submissionId,
//            gameId = gameId,
//        ) ?: throw CustomException(ErrorCode.GAME_SUBMISSION_NOT_FOUND)
//
//        // 점수 매핑
//        val winnerScore = scoreOf(submission, submission.winner.id!!)
//        val loserScore = scoreOf(submission, submission.loser.id!!)
//
//        return GameResultSubmissionDetailResponse.from(
//            submission = submission,
//            winnerScore = winnerScore,
//            loserScore = loserScore,
//        )
//    }

    @Transactional(readOnly = true)
    fun getPendingResultAcceptedGames(
        userId: String,
        request: CommonCursorRequest,
    ): CursorResponse<PendingResultAcceptedGameSummaryResponse> {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val activeProfileId = user.activeUserSportProfileId
            ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)

        val activeProfile = userSportProfileRepository.findByIdOrNull(activeProfileId)
            ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)

        val sportId = activeProfile.sport.id
            ?: throw CustomException(ErrorCode.SPORT_NOT_FOUND)

        val snapshotAt = request.snapshotAt ?: TimeUtils.nowOffsetDateTime()

        val response = gameRepository.fetchPendingResultAcceptedGamesPage(
            userId = userId,
            sportId = sportId,
            request = request,
            snapshotAt = snapshotAt,
        )

        val now = LocalDateTime.now(TimeUtils.DEFAULT_ZONE_ID)
        val startOfDay = now.toLocalDate().atStartOfDay()

        val results = response.results.map { projection ->
            val availableAt = calcSubmitAvailableAt(
                now = now,
                startOfDay = startOfDay,
                gameCreatedAt = projection.createdAtLdt,
                requesterId = projection.requesterUserId,
                receiverId = projection.receiverUserId,
            )

            val lockResponse = GameResultSubmitLockResponse.from(
                now = now,
                availableAt = now, //availableAt, // TODO: 앱잼 기간 내 잠금 정책 제외
            )

            PendingResultAcceptedGameSummaryResponse.from(
                projection = projection,
                submitAvailableAt = lockResponse.submitAvailableAt,
                remainingSeconds = lockResponse.remainingSeconds,
                isSubmitLocked = lockResponse.isLocked,
            )
        }

        return CursorResponse(
            snapshotAt = response.snapshotAt,
            results = results,
            nextCursor = response.nextCursor,
            hasNext = response.hasNext,
        )
    }

    /**
     * 오늘 기준(00:00~)
     * - 오늘 확정 0건이면(= 오늘 첫 확정 후보): 생성 후 1시간 제출 불가
     * - 오늘 확정 1~2건이면(= 오늘 2~3번째 확정 후보):
     *   직전 확정이 30분 이내에 있었던 연속 확정 상황일 때만 생성 후 10분 제출 불가
     */
    private fun calcSubmitAvailableAt(
        now: LocalDateTime,
        startOfDay: LocalDateTime,
        gameCreatedAt: LocalDateTime,
        requesterId: String,
        receiverId: String,
    ): LocalDateTime {

        val todayConfirmedCount = gameRepository.countTodayConfirmedGamesBetweenUsers(
            startAt = startOfDay,
            userA = requesterId,
            userB = receiverId,
        )

        // 오늘 첫 확정 후보 → 생성 후 1시간 제한
        if (todayConfirmedCount == 0L) {
            return gameCreatedAt.plusHours(1)
        }

        // 오늘 2~3번째 확정 후보 → 연속 확정일 때만 10분 제한
        if (todayConfirmedCount in 1L..2L) {
            val prevConfirmedAt = gameRepository.findTodayLatestConfirmedAtBetweenUsers(
                startAt = startOfDay,
                userA = requesterId,
                userB = receiverId,
            ) ?: return gameCreatedAt

            if (ChronoUnit.MINUTES.between(prevConfirmedAt, now) <= 30) {
                return gameCreatedAt.plusMinutes(10)
            }
        }

        return gameCreatedAt
    }

    /**
     * LP 업데이트 후 티어 재산정 및 LP 변동 내역 저장
      - 승자: +30 LP / 패자: -20 LP(최소 0)
      - 티어는 LP 기준으로 resolveTierOrThrow() 통해 재산정
     */
    private fun applyLpAndTierUpdate(
        winnerProfile: UserSportProfile,
        loserProfile: UserSportProfile,
        sportId: Long,
        game: Game,
    ) {
        // 경기 결과에 따른 승/패 기록 업데이트
        winnerProfile.recordWin()
        loserProfile.recordLoss()

        val winnerBefore = winnerProfile.lp
        val loserBefore = loserProfile.lp

        val winnerDelta = 30
        val loserDelta = 20

        // LP 업데이트: 승자 +30 / 패자 -20(최소 0)
        val winnerAfter = winnerBefore + winnerDelta
        val loserAfter = (loserBefore - loserDelta).coerceAtLeast(0)

        winnerProfile.lp = winnerAfter
        loserProfile.lp = loserAfter

        // 티어 업데이트: LP 기준으로 티어 재산정
        winnerProfile.changeTier(resolveTierOrThrow(sportId, winnerAfter))
        loserProfile.changeTier(resolveTierOrThrow(sportId, loserAfter))

        // LP 변동 내역 저장
        lpHistoryRepository.save(
            LpHistory.create(
                userSportProfile = winnerProfile,
                game = game,
                beforeLp = winnerBefore,
                deltaLp = winnerDelta,
                afterLp = winnerAfter,
            )
        )

        lpHistoryRepository.save(
            LpHistory.create(
                userSportProfile = loserProfile,
                game = game,
                beforeLp = loserBefore,
                deltaLp = -loserDelta,
                afterLp = loserAfter,
            )
        )
    }

    /**
     * 주어진 스포츠 ID와 LP에 해당하는 티어 조회
      - 티어가 존재하지 않을 경우 예외 발생
     */
    private fun resolveTierOrThrow(
        sportId: Long,
        lp: Int,
    ): Tier {
        return tierRepository.findBySportIdAndLpInRange(sportId, lp)
            ?: throw CustomException(ErrorCode.TIER_NOT_FOUND)
    }
}
