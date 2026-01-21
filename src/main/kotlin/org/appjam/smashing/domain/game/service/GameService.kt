package org.appjam.smashing.domain.game.service

import org.appjam.smashing.domain.game.dto.command.GameResultConfirmCommand
import org.appjam.smashing.domain.game.dto.command.GameResultRejectCommand
import org.appjam.smashing.domain.game.dto.command.GameResultSubmitCommand
import org.appjam.smashing.domain.game.dto.response.GameResultConfirmResponse
import org.appjam.smashing.domain.game.dto.response.GameResultSubmissionDetailResponse
import org.appjam.smashing.domain.game.dto.response.GameResultSubmitLockResponse
import org.appjam.smashing.domain.game.dto.response.GameResultSubmitResponse
import org.appjam.smashing.domain.game.dto.response.PendingResultAcceptedGameSummaryResponse
import org.appjam.smashing.domain.game.entity.Game
import org.appjam.smashing.domain.game.entity.GameResultSubmission
import org.appjam.smashing.domain.game.enums.GameResultRejectReason
import org.appjam.smashing.domain.game.enums.GameResultStatus
import org.appjam.smashing.domain.game.enums.SubmissionStatus
import org.appjam.smashing.domain.game.repository.GameRepository
import org.appjam.smashing.domain.game.repository.GameResultSubmissionRepository
import org.appjam.smashing.domain.lp.entity.LpHistory
import org.appjam.smashing.domain.lp.repository.LpHistoryRepository
import org.appjam.smashing.domain.notification.enums.NotificationType
import org.appjam.smashing.domain.notification.service.NotificationService
import org.appjam.smashing.domain.outbox.components.OutboxEventPublisher
import org.appjam.smashing.domain.outbox.dto.GameResultRejectedNotificationCreatedPayload
import org.appjam.smashing.domain.outbox.dto.GameResultSubmittedNotificationCreatedPayload
import org.appjam.smashing.domain.outbox.dto.GameUpdatedPayload
import org.appjam.smashing.domain.outbox.dto.ReviewReceivedNotificationCreatedPayload
import org.appjam.smashing.domain.outbox.enums.SseEventType
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
        // 게임 조회(잠금)
        val game = gameRepository.findByIdFetchAllForUpdate(gameId)
            ?: throw CustomException(ErrorCode.GAME_NOT_FOUND)

        // 게임 상태 검증
        if (game.resultStatus != GameResultStatus.PENDING_RESULT && game.resultStatus != GameResultStatus.RESULT_REJECTED) {
            throw CustomException(ErrorCode.GAME_RESULT_ALREADY_SUBMITTED)
        }

        // 제출자/상대 결정
        val requester = game.matching.requester
        val receiver = game.matching.receiver
        val (submitter, confirmer) = determineSubmitterAndConfirmer(submitterUserId, requester, receiver)

        val submitterProfile = userSportProfileRepository.findByUserIdAndSportIdFetch(
            userId = submitter.id!!,
            sportId = game.sport.id!!,
        ) ?: throw CustomException(ErrorCode.MATCHING_REQUESTER_NOT_FOUND)

        val confirmerProfile = userSportProfileRepository.findByUserIdAndSportIdFetch(
            userId = confirmer.id!!,
            sportId = game.sport.id!!,
        ) ?: throw CustomException(ErrorCode.MATCHING_RECEIVER_PROFILE_NOT_FOUND)

        // 게임 결과 제출 시간 제약
        validateSubmitWindow(game)

        val totalSubmissionCount = submissionRepository.countByGame_Id(gameId)
        val submitterSubmissionCount = submissionRepository.countByGame_IdAndSubmitter_Id(gameId, submitterUserId)

        // 재제출 제약 (이전 제출자만 가능)
        if (totalSubmissionCount >= 1L && submitterSubmissionCount == 0L) {
            throw CustomException(ErrorCode.GAME_RESULT_RESUBMIT_ONLY_PREVIOUS_SUBMITTER)
        }

        // 유저당 제출 2회 제한
        if (submitterSubmissionCount >= 2L) {
            throw CustomException(ErrorCode.GAME_RESULT_SUBMISSION_LIMIT_EXCEEDED)
        }

        // attemptNo 계산
        val attemptNo = (totalSubmissionCount + 1).toInt()

        // 리뷰 검증 (첫 제출 시 필수, 재제출 시 불가)
        validateReviewRule(attemptNo, command.review)

        // score 검증, winner/loser 검증
        validateWinnerLoserAndScores(
            winnerUserId = command.winnerUserId,
            loserUserId = command.loserUserId,
            scoreWinner = command.scoreWinner,
            scoreLoser = command.scoreLoser,
            requesterUserId = requester.id!!,
            receiverUserId = receiver.id!!,
        )

        // winner/loser 엔티티 매핑
        val (winner, loser) = determineWinnerAndLoser(game, command.winnerUserId, command.loserUserId)

        // 게임 상태 변경
        game.markWaitingConfirmation()

        // 경기 결과 제출안 저장
        val submission = submissionRepository.save(
            GameResultSubmission.create(
                game = game,
                submitter = submitter,
                confirmer = confirmer,
                winner = winner,
                loser = loser,
                attemptNo = attemptNo,
                scoreSubmitter = if (submitter.id == command.winnerUserId) command.scoreWinner else command.scoreLoser,
                scoreConfirmer = if (confirmer.id == command.winnerUserId) command.scoreWinner else command.scoreLoser,
            )
        )

        // 게임 상태 변경 SSE 발행
        publishGameUpdated(
            receiverUserId = confirmer.id!!,
            gameId = gameId,
            resultStatus = game.resultStatus
        )

        // 결과 제출 알림 + SSE 발행
        notifyGameResultSubmitted(
            receiver = confirmer,
            receiverProfile = confirmerProfile,
            game = game,
            submission = submission,
            submitter = submitter,
            submitterTierCode = submitterProfile.tier.code,
        )

        // 후기 저장 + 후기 제출 알림 + SSE 발행
        val reviewId = if (attemptNo == 1) {
            notifyReviewReceived(
                game = game,
                reviewer = submitter,
                reviewee = confirmer,
                receiverProfile = confirmerProfile,
                review = command.review!!,
                reviewerTierCode = submitterProfile.tier.code,
            )
        } else {
            null
        }

        return GameResultSubmitResponse.from(reviewId)
    }

    @Transactional
    fun confirmResult(
        confirmerUserId: String,
        gameId: String,
        submissionId: String,
        command: GameResultConfirmCommand,
    ): GameResultConfirmResponse {
        val now = LocalDateTime.now(DEFAULT_ZONE_ID)

        // 게임 조회(잠금)
        val game = gameRepository.findByIdFetchAllForUpdate(gameId)
            ?: throw CustomException(ErrorCode.GAME_NOT_FOUND)

        // 상태 검증
        if (game.resultStatus != GameResultStatus.WAITING_CONFIRMATION) {
            throw CustomException(ErrorCode.GAME_RESULT_NOT_WAITING_CONFIRMATION)
        }

        // 제출안 조회(잠금)
        val submission = submissionRepository.findByIdAndGameIdForUpdate(submissionId, gameId)
            ?: throw CustomException(ErrorCode.GAME_SUBMISSION_NOT_FOUND)

        // 제출안 상태 검증
        if (submission.status != SubmissionStatus.SUBMITTED) {
            throw CustomException(ErrorCode.GAME_SUBMISSION_NOT_SUBMITTED)
        }

        // confirmer 검증
        if (submission.confirmer.id != confirmerUserId) {
            throw CustomException(ErrorCode.GAME_SUBMISSION_CONFIRMER_MISMATCH)
        }

        // 확정 점수 매핑
        val scoreWinner = if (submission.winner.id == submission.submitter.id) submission.scoreSubmitter else submission.scoreConfirmer
        val scoreLoser = if (submission.loser.id == submission.submitter.id) submission.scoreSubmitter else submission.scoreConfirmer

        // game 확정 + submission 수락
        game.confirmResult(
            submissionId = submission.id!!,
            winner = submission.winner,
            loser = submission.loser,
            scoreWinner = scoreWinner,
            scoreLoser = scoreLoser,
            confirmedAt = now,
        )
        submission.accept(now)

        // 승자/패자 프로필 조회(잠금)
        val sportId = game.sport.id!!
        val submitterProfile = userSportProfileRepository.findByUserIdAndSportIdForUpdate(submission.submitter.id!!, sportId)
            ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)

        val confirmerProfile = userSportProfileRepository
            .findByUserIdAndSportIdForUpdate(submission.confirmer.id!!, sportId)
            ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)

        // 승자/패자 프로필 업데이트 (승리/패배 수 + LP + 티어 + LP history)
        val winnerProfile = if (submission.winner.id == submission.submitter.id) submitterProfile else confirmerProfile
        val loserProfile = if (submission.loser.id == submission.submitter.id) submitterProfile else confirmerProfile

        applyLpAndTierUpdate(winnerProfile, loserProfile, sportId, game)

        // 게임 상태 변경 SSE 발행
        publishGameUpdated(
            receiverUserId = submission.submitter.id!!,
            gameId = game.id!!,
            resultStatus = game.resultStatus,
        )

        // 후기 저장 + 후기 제출 알림 + SSE 발행
        val reviewId = notifyReviewReceivedOnConfirm(
            game = game,
            reviewer = submission.confirmer,
            reviewee = submission.submitter,
            receiverProfile = submitterProfile,
            review = command.review,
            reviewerTierCode = confirmerProfile.tier.code,
        )

        return GameResultConfirmResponse.from(reviewId)
    }

    @Transactional(readOnly = true)
    fun getSubmissionDetail(
        gameId: String,
        submissionId: String,
    ): GameResultSubmissionDetailResponse {
        val submission = submissionRepository.findDetailByIdAndGameId(
            submissionId = submissionId,
            gameId = gameId,
        ) ?: throw CustomException(ErrorCode.GAME_SUBMISSION_NOT_FOUND)

        // 점수 매핑
        val winnerScore = scoreOf(submission, submission.winner.id!!)
        val loserScore = scoreOf(submission, submission.loser.id!!)

        return GameResultSubmissionDetailResponse.from(
            submission = submission,
            winnerScore = winnerScore,
            loserScore = loserScore,
        )
    }

    @Transactional
    fun rejectResult(
        confirmerUserId: String,
        gameId: String,
        submissionId: String,
        command: GameResultRejectCommand,
    ) {
        val now = LocalDateTime.now(DEFAULT_ZONE_ID)// TODO: 인증인가 회복시 변경

        // 게임 조회(잠금)
        val game = gameRepository.findByIdForUpdate(gameId)
            ?: throw CustomException(ErrorCode.GAME_NOT_FOUND)

        // 경기 상태 검증
        if (game.resultStatus != GameResultStatus.WAITING_CONFIRMATION) {
            throw CustomException(ErrorCode.GAME_RESULT_NOT_WAITING_CONFIRMATION)
        }

        // 제출안 조회(잠금)
        val submission = submissionRepository.findByIdAndGameIdForUpdate(submissionId, gameId)
            ?: throw CustomException(ErrorCode.GAME_SUBMISSION_NOT_FOUND)

        // 제출안 상태 검증
        if (submission.status != SubmissionStatus.SUBMITTED) {
            throw CustomException(ErrorCode.GAME_SUBMISSION_NOT_SUBMITTED)
        }

        // confirmer 검증
        if (submission.confirmer.id != confirmerUserId) {
            throw CustomException(ErrorCode.GAME_SUBMISSION_CONFIRMER_MISMATCH)
        }

        // 재제출 가능하도록 게임 상태를 REJECTED로
        game.markRejected()

        // 상태 변경 SSE는 항상 보내도록
        publishGameUpdated(
            receiverUserId = submission.submitter.id!!,
            gameId = game.id!!,
            resultStatus = game.resultStatus,
        )

        if (submission.attemptNo == 1) {
            // 1회차 반려: 사유 필수 + 알림/SSE 발행
            val reason = command.reason ?: throw CustomException(ErrorCode.GAME_RESULT_REJECT_REASON_REQUIRED_ON_FIRST_REJECT)

            submission.rejectWithReason(
                reason = reason,
                actedAt = now,
            )

            val receiverProfile = userSportProfileRepository.findByUserIdAndSportIdFetch(
                userId = submission.submitter.id!!,
                sportId = game.sport.id!!,
            ) ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)

            val rejectorProfile = userSportProfileRepository.findByUserIdAndSportIdFetch(
                userId = submission.confirmer.id!!,
                sportId = game.sport.id!!,
            ) ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)

            notifyGameResultRejected(
                receiver = submission.submitter,
                receiverProfile = receiverProfile,
                rejector = submission.confirmer,
                rejectorTierCode = rejectorProfile.tier.code,
                gameId = game.id!!,
                submissionId = submission.id!!,
                reason = reason,
            )
            return
        }

        // 2회차 이상 반려: 제출안 삭제 + 알림/SSE 없음
        submissionRepository.delete(submission)
    }

    @Transactional
    fun deleteGame(
        userId: String,
        gameId: String,
    ) {
        // 게임 조회(잠금)
        val game = gameRepository.findByIdFetchUsersForUpdate(gameId)
            ?: throw CustomException(ErrorCode.GAME_NOT_FOUND)

        // 삭제 가능 상태 검증
        validateDeletable(game.resultStatus)

        // 상대방 userId 조회
        val opponentUserId = resolveOpponentUserId(
            requesterId = game.matching.requester.id!!,
            receiverId = game.matching.receiver.id!!,
            userId = userId,
        )

        // 게임 취소 처리
        game.cancel()

        /* 앱잼 기간 내 삭제 기능 제외
        gameRepository.flush()

        // submissions soft delete
        submissionRepository.softDeleteAllByGameId(gameId)

        // game soft delete
        gameRepository.delete(game)

        // 게임 상태 변경 SSE 발행
        publishGameUpdated(
            receiverUserId = opponentUserId,
            gameId = game.id!!,
            resultStatus = game.resultStatus,
        )
        */
    }

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
                availableAt = availableAt,
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

    private fun validateDeletable(
        resultStatus: GameResultStatus
    ) {
        if (resultStatus == GameResultStatus.RESULT_CONFIRMED) {
            throw CustomException(ErrorCode.GAME_RESULT_ALREADY_CONFIRMED)
        }
    }

    private fun resolveOpponentUserId(
        requesterId: String,
        receiverId: String,
        userId: String,
    ): String {
        return when (userId) {
            requesterId -> receiverId
            receiverId -> requesterId
            else -> throw CustomException(ErrorCode.GAME_FORBIDDEN)
        }
    }

    private fun scoreOf(
        submission: GameResultSubmission,
        userId: String,
    ): Int {
        return when (userId) {
            submission.submitter.id!! -> submission.scoreSubmitter
            submission.confirmer.id!! -> submission.scoreConfirmer
            else -> throw CustomException(ErrorCode.GAME_RESULT_INVALID_PLAYERS)
        }
    }

    private fun determineSubmitterAndConfirmer(
        submitterUserId: String,
        requester: User,
        receiver: User,
    ): Pair<User, User> {
        if (submitterUserId != requester.id!! && submitterUserId != receiver.id!!) {
            throw CustomException(ErrorCode.GAME_FORBIDDEN)
        }
        return if (submitterUserId == requester.id!!) requester to receiver else receiver to requester
    }

    private fun determineWinnerAndLoser(
        game: Game,
        winnerUserId: String,
        loserUserId: String,
    ): Pair<User, User> {
        val requester = game.matching.requester
        val receiver = game.matching.receiver

        val winner = if (winnerUserId == requester.id!!) requester else receiver
        val loser = if (loserUserId == requester.id!!) requester else receiver

        return winner to loser
    }

    private fun validateReviewRule(
        attemptNo: Int,
        review: GameResultSubmitCommand.ReviewCommand?,
    ) {
        if (attemptNo == 1 && review == null) {
            throw CustomException(ErrorCode.GAME_REVIEW_REQUIRED_ON_FIRST_SUBMISSION)
        }
        if (attemptNo != 1 && review != null) {
            throw CustomException(ErrorCode.GAME_REVIEW_ONLY_FIRST_SUBMISSION_ALLOWED)
        }
    }

    private fun validateWinnerLoserAndScores(
        winnerUserId: String,
        loserUserId: String,
        scoreWinner: Int,
        scoreLoser: Int,
        requesterUserId: String,
        receiverUserId: String,
    ) {
        if (winnerUserId == loserUserId) throw CustomException(ErrorCode.GAME_RESULT_SAME_PLAYER)
        if (scoreWinner <= scoreLoser) throw CustomException(ErrorCode.GAME_RESULT_INVALID_SCORE)

        if ((winnerUserId != requesterUserId && winnerUserId != receiverUserId) || (loserUserId != requesterUserId && loserUserId != receiverUserId)) {
            throw CustomException(ErrorCode.GAME_RESULT_INVALID_PLAYERS)
        }
    }

    /**
     * 오늘 기준(00:00~)
     * - 오늘 확정 0건이면(= 오늘 첫 확정 후보): 생성 후 1시간 제출 불가
     * - 오늘 확정 1~2건이면(= 오늘 2~3번째 확정 후보):
     *   직전 확정이 30분 이내에 있었던 연속 확정 상황일 때만
     *   생성 후 10분 제출 불가
     */
    private fun validateSubmitWindow(game: Game) {
        val now = LocalDateTime.now(DEFAULT_ZONE_ID)
        val startOfDay = now.toLocalDate().atStartOfDay()

        val requesterId = game.matching.requester.id!!
        val receiverId = game.matching.receiver.id!!

        val todayConfirmedCount = gameRepository.countTodayConfirmedGamesBetweenUsers(
            startAt = startOfDay,
            userA = requesterId,
            userB = receiverId,
        )

        // 오늘 첫 확정 후보 → 1시간 결과 제출 제한
        if (todayConfirmedCount == 0L) {
            if (ChronoUnit.MINUTES.between(game.createdAt, now) < 60) {
                throw CustomException(ErrorCode.GAME_RESULT_SUBMIT_BLOCKED_1H)
            }
            return
        }

        // 오늘 2~3번째 확정 후보 → 연속 확정일 때만 10분 룰
        if (todayConfirmedCount in 1L..2L) {
            val prevConfirmedAt = gameRepository.findTodayLatestConfirmedAtBetweenUsers(
                startAt = startOfDay,
                userA = requesterId,
                userB = receiverId,
            ) ?: return

            // 직전 확정 이후 30분 이내에 게임 → 현재 게임은 생성 후 10분 결과 제출 제한
            if (ChronoUnit.MINUTES.between(prevConfirmedAt, now) <= 30) {
                if (ChronoUnit.MINUTES.between(game.createdAt, now) < 10) {
                    throw CustomException(ErrorCode.GAME_RESULT_SUBMIT_BLOCKED_10M)
                }
            }
        }
    }

    private fun notifyGameResultSubmitted(
        receiver: User,
        receiverProfile: UserSportProfile,
        game: Game,
        submission: GameResultSubmission,
        submitter: User,
        submitterTierCode: TierCode,
    ) {
        val notification = notificationService.createMatchingResultSubmitted(
            receiver = receiver,
            receiverProfile = receiverProfile,
            submitterNickname = submitter.nickname,
            game = game,
            submission = submission,
        )

        val notificationCreatedAt = notification.createdAt
            .atZone(DEFAULT_ZONE_ID)
            .toOffsetDateTime()
            .toString()

        outboxEventPublisher.publish(
            userId = receiver.id!!,
            eventType = SseEventType.GAME_RESULT_SUBMITTED_NOTIFICATION_CREATED,
            payload = GameResultSubmittedNotificationCreatedPayload(
                notificationId = notification.id!!,
                notificationType = NotificationType.MATCHING_RESULT_SUBMITTED,
                notificationCreatedAt = notificationCreatedAt,
                sportId = game.sport.id!!,
                receiverProfileId = receiverProfile.id!!,
                gameId = game.id!!,
                submissionId = submission.id!!,
                submitter = GameResultSubmittedNotificationCreatedPayload.SubmitterSummary(
                    userId = submitter.id!!,
                    nickname = submitter.nickname,
                    tierCode = submitterTierCode,
                )
            )
        )
    }

    private fun notifyReviewReceived(
        game: Game,
        reviewer: User,
        reviewee: User,
        receiverProfile: UserSportProfile,
        review: GameResultSubmitCommand.ReviewCommand,
        reviewerTierCode: TierCode,
    ): String {
        val savedReview = gameReviewService.createReview(
            gameId = game.id!!,
            reviewer = reviewer,
            reviewee = reviewee,
            rating = review.rating,
            content = review.content,
            tags = review.tags,
        )

        val notification = notificationService.createReviewReceived(
            receiver = reviewee,
            receiverProfile = receiverProfile,
            reviewId = savedReview.id!!,
            reviewerNickname = reviewer.nickname,
        )

        val notificationCreatedAt = notification.createdAt
            .atZone(DEFAULT_ZONE_ID)
            .toOffsetDateTime()
            .toString()

        outboxEventPublisher.publish(
            userId = reviewee.id!!,
            eventType = SseEventType.REVIEW_RECEIVED_NOTIFICATION_CREATED,
            payload = ReviewReceivedNotificationCreatedPayload(
                notificationId = notification.id!!,
                notificationType = NotificationType.REVIEW_RECEIVED,
                notificationCreatedAt = notificationCreatedAt,
                sportId = game.sport.id!!,
                receiverProfileId = receiverProfile.id!!,
                gameId = game.id!!,
                reviewId = savedReview.id!!,
                reviewer = ReviewReceivedNotificationCreatedPayload.ReviewerSummary(
                    userId = reviewer.id!!,
                    nickname = reviewer.nickname,
                    tierCode = reviewerTierCode,
                )
            )
        )

        return savedReview.id!!
    }

    private fun notifyReviewReceivedOnConfirm(
        game: Game,
        reviewer: User,
        reviewee: User,
        receiverProfile: UserSportProfile,
        review: GameResultConfirmCommand.ReviewCommand,
        reviewerTierCode: TierCode,
    ): String {
        val savedReview = gameReviewService.createReview(
            gameId = game.id!!,
            reviewer = reviewer,
            reviewee = reviewee,
            rating = review.rating,
            content = review.content,
            tags = review.tags,
        )

        val notification = notificationService.createReviewReceived(
            receiver = reviewee,
            receiverProfile = receiverProfile,
            reviewId = savedReview.id!!,
            reviewerNickname = reviewer.nickname,
        )

        val notificationCreatedAt = notification.createdAt
            .atZone(DEFAULT_ZONE_ID)
            .toOffsetDateTime()
            .toString()

        outboxEventPublisher.publish(
            userId = reviewee.id!!,
            eventType = SseEventType.REVIEW_RECEIVED_NOTIFICATION_CREATED,
            payload = ReviewReceivedNotificationCreatedPayload(
                notificationId = notification.id!!,
                notificationType = NotificationType.REVIEW_RECEIVED,
                notificationCreatedAt = notificationCreatedAt,
                sportId = game.sport.id!!,
                receiverProfileId = receiverProfile.id!!,
                gameId = game.id!!,
                reviewId = savedReview.id!!,
                reviewer = ReviewReceivedNotificationCreatedPayload.ReviewerSummary(
                    userId = reviewer.id!!,
                    nickname = reviewer.nickname,
                    tierCode = reviewerTierCode,
                )
            )
        )

        return savedReview.id!!
    }

    private fun publishGameUpdated(
        receiverUserId: String,
        gameId: String,
        resultStatus: GameResultStatus,
    ) {
        outboxEventPublisher.publish(
            userId = receiverUserId,
            eventType = SseEventType.GAME_UPDATED,
            payload = GameUpdatedPayload(
                gameId = gameId,
                resultStatus = resultStatus,
            )
        )
    }

    private fun notifyGameResultRejected(
        receiver: User,
        receiverProfile: UserSportProfile,
        rejector: User,
        rejectorTierCode: TierCode,
        gameId: String,
        submissionId: String,
        reason: GameResultRejectReason,
    ) {
        val notificationType = when (reason) {
            GameResultRejectReason.SCORE_MISMATCH -> NotificationType.RESULT_REJECTED_SCORE_MISMATCH
            GameResultRejectReason.WIN_LOSE_REVERSED -> NotificationType.RESULT_REJECTED_WIN_LOSE_REVERSED
            GameResultRejectReason.SCORE_AND_WIN_LOSE_MISMATCH -> NotificationType.RESULT_REJECTED_SCORE_AND_WIN_LOSE_MISMATCH
            GameResultRejectReason.GAME_NOT_PLAYED_YET -> NotificationType.RESULT_REJECTED_GAME_NOT_PLAYED_YET
        }

        val notification = notificationService.createResultRejected(
            receiver = receiver,
            receiverProfile = receiverProfile,
            notificationType = notificationType,
            rejectorNickname = rejector.nickname,
        )

        val notificationCreatedAt = notification.createdAt
            .atZone(DEFAULT_ZONE_ID)
            .toOffsetDateTime()
            .toString()

        outboxEventPublisher.publish(
            userId = receiver.id!!,
            eventType = SseEventType.GAME_RESULT_REJECTED_NOTIFICATION_CREATED,
            payload = GameResultRejectedNotificationCreatedPayload(
                notificationId = notification.id!!,
                notificationType = notificationType,
                notificationCreatedAt = notificationCreatedAt,
                sportId = receiverProfile.sport.id!!,
                receiverProfileId = receiverProfile.id!!,
                gameId = gameId,
                submissionId = submissionId,
                reason = reason,
                rejector = GameResultRejectedNotificationCreatedPayload.RejectorSummary(
                    userId = rejector.id!!,
                    nickname = rejector.nickname,
                    tierCode = rejectorTierCode,
                ),
            )
        )
    }

    private fun applyLpAndTierUpdate(
        winnerProfile: UserSportProfile,
        loserProfile: UserSportProfile,
        sportId: Long,
        game: Game,
    ) {
        // 승/패 수 갱신
        winnerProfile.recordWin()
        loserProfile.recordLoss()

        // 이번 경기 기준 gameNo 계산 (업데이트 후 wins+losses 기준)
        val winnerGameNo = winnerProfile.wins + winnerProfile.losses
        val loserGameNo = loserProfile.wins + loserProfile.losses

        // 이번 경기 LP 변화량 계산
        val winnerDelta = calcWinLpDelta(winnerGameNo)
        val loserDelta = calcLoseLpDelta(loserGameNo)

        // before/after 계산 (0 미만 방지 포함)
        val winnerBefore = winnerProfile.lp
        val loserBefore = loserProfile.lp

        val winnerAfter = winnerBefore + winnerDelta
        val loserAfter = (loserBefore - loserDelta).coerceAtLeast(0)

        // 실제 LP 반영
        winnerProfile.lp = winnerAfter
        loserProfile.lp = loserAfter

        // tier 재계산/갱신
        winnerProfile.changeTier(resolveTierOrThrow(sportId, winnerAfter))
        loserProfile.changeTier(resolveTierOrThrow(sportId, loserAfter))

        // LP history 2건 저장 (승자/패자 각각 1건)
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

    private fun calcWinLpDelta(
        gameNo: Int
    ): Int {
        return when (gameNo) {
            in 1..3 -> 90
            in 4..8 -> 45
            else -> 30
        }
    }

    private fun calcLoseLpDelta(
        gameNo: Int
    ): Int {
        return when (gameNo) {
            in 1..3 -> 20
            in 4..8 -> 15
            else -> 20
        }
    }

    private fun resolveTierOrThrow(
        sportId: Long,
        lp: Int
    ): Tier {
        return tierRepository.findBySportIdAndLpInRange(sportId, lp)
            ?: throw CustomException(ErrorCode.TIER_NOT_FOUND) // TODO: 추후 챌린저 관련 maxlp 처리 필요
    }

    companion object {
        private val DEFAULT_ZONE_ID = ZoneId.of("Asia/Seoul")
    }
}
