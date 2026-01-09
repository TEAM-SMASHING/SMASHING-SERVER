package org.appjam.smashing.domain.game.service

import org.appjam.smashing.domain.game.dto.command.GameResultConfirmCommand
import org.appjam.smashing.domain.game.dto.command.GameResultSubmitCommand
import org.appjam.smashing.domain.game.dto.response.GameResultSubmissionDetailResponse
import org.appjam.smashing.domain.game.entity.Game
import org.appjam.smashing.domain.game.entity.GameResultSubmission
import org.appjam.smashing.domain.game.enums.GameResultStatus
import org.appjam.smashing.domain.game.enums.SubmissionStatus
import org.appjam.smashing.domain.game.repository.GameRepository
import org.appjam.smashing.domain.game.repository.GameResultSubmissionRepository
import org.appjam.smashing.domain.notification.enums.NotificationType
import org.appjam.smashing.domain.notification.service.NotificationService
import org.appjam.smashing.domain.outbox.components.OutboxEventPublisher
import org.appjam.smashing.domain.outbox.dto.GameResultSubmittedNotificationCreatedPayload
import org.appjam.smashing.domain.outbox.dto.GameUpdatedPayload
import org.appjam.smashing.domain.outbox.dto.ReviewReceivedNotificationCreatedPayload
import org.appjam.smashing.domain.outbox.enums.SseEventType
import org.appjam.smashing.domain.review.service.GameReviewService
import org.appjam.smashing.domain.tier.entity.Tier
import org.appjam.smashing.domain.tier.repository.TierRepository
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.appjam.smashing.domain.user.repository.UserSportProfileRepository
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Service
class GameService(
    private val gameRepository: GameRepository,
    private val submissionRepository: GameResultSubmissionRepository,
    private val notificationService: NotificationService,
    private val outboxEventPublisher: OutboxEventPublisher,
    private val gameReviewService: GameReviewService,
    private val userSportProfileRepository: UserSportProfileRepository,
    private val tierRepository: TierRepository,
) {

    @Transactional
    fun submitResult(
        submitterUserId: String,
        gameId: String,
        command: GameResultSubmitCommand,
    ) {
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

        // 유저당 제출 2회 제한
        if (submissionRepository.countByGame_IdAndSubmitter_Id(gameId, submitterUserId) >= 2L) {
            throw CustomException(ErrorCode.GAME_RESULT_SUBMISSION_LIMIT_EXCEEDED)
        }

        // 게임 결과 제출 시간 제약
        validateSubmitWindow(game)

        // attemptNo = 해당 게임 제출 순번 계산
        val attemptNo = (submissionRepository.countByGame_Id(gameId) + 1).toInt()

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
            game = game,
            submission = submission,
            submitter = submitter,
            receiverProfileId = confirmerProfile.id!!,
            submitterTierId = submitterProfile.tier.id!!,
        )

        // 후기 저장 + 후기 제출 알림 + SSE 발행
        if (attemptNo == 1) {
            notifyReviewReceived(
                game = game,
                reviewer = submitter,
                reviewee = confirmer,
                review = command.review!!,
                receiverProfileId = confirmerProfile.id!!,
                reviewerTierId = submitterProfile.tier.id!!,
            )
        }
    }

    @Transactional
    fun confirmResult(
        confirmerUserId: String,
        gameId: String,
        submissionId: String,
        command: GameResultConfirmCommand,
    ) {
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

        // review 정책 검증
        validateConfirmReviewRule(
            attemptNo = submission.attemptNo,
            review = command.review,
        )

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

        // 승자/패자 프로필 업데이트 (승리/패배 수 + LP + 티어)
        val winnerProfile = if (submission.winner.id == submission.submitter.id) submitterProfile else confirmerProfile
        val loserProfile = if (submission.loser.id == submission.submitter.id) submitterProfile else confirmerProfile

        applyLpAndTierUpdate(winnerProfile, loserProfile, sportId)

        // 게임 상태 변경 SSE 발행
        publishGameUpdated(
            receiverUserId = submission.submitter.id!!,
            gameId = game.id!!,
            resultStatus = game.resultStatus,
        )

        // 후기 저장 + 후기 제출 알림 + SSE 발행
        if (submission.attemptNo == 1) {
            notifyReviewReceivedOnConfirm(
                game = game,
                reviewer = submission.confirmer,
                reviewee = submission.submitter,
                review = command.review!!,
                receiverProfileId = submitterProfile.id!!,
                reviewerTierId = confirmerProfile.tier.id!!,
            )
        }
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

    private fun validateConfirmReviewRule(
        attemptNo: Int,
        review: GameResultConfirmCommand.ReviewCommand?,
    ) {
        if (attemptNo == 1 && review == null) {
            throw CustomException(ErrorCode.GAME_REVIEW_REQUIRED_ON_FIRST_SUBMISSION)
        }
        if (attemptNo != 1 && review != null) {
            throw CustomException(ErrorCode.GAME_REVIEW_ONLY_FIRST_SUBMISSION_ALLOWED)
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
        game: Game,
        submission: GameResultSubmission,
        submitter: User,
        receiverProfileId: String,
        submitterTierId: Long,
    ) {
        val notification = notificationService.createMatchingResultSubmitted(
            receiver = receiver,
            gameId = game.id!!,
            submissionId = submission.id!!,
            submitterNickname = submitter.nickname,
            submitterTierId = submitterTierId,
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
                receiverProfileId = receiverProfileId,
                gameId = game.id!!,
                submissionId = submission.id!!,
                submitter = GameResultSubmittedNotificationCreatedPayload.SubmitterSummary(
                    userId = submitter.id!!,
                    nickname = submitter.nickname,
                    tierId = submitterTierId,
                )
            )
        )
    }

    private fun notifyReviewReceived(
        game: Game,
        reviewer: User,
        reviewee: User,
        review: GameResultSubmitCommand.ReviewCommand,
        receiverProfileId: String,
        reviewerTierId: Long,
    ) {
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
            reviewId = savedReview.id!!,
            reviewerNickname = reviewer.nickname,
            reviewerTierId = reviewerTierId,
            gameId = game.id!!,
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
                receiverProfileId = receiverProfileId,
                gameId = game.id!!,
                reviewId = savedReview.id!!,
                reviewer = ReviewReceivedNotificationCreatedPayload.ReviewerSummary(
                    userId = reviewer.id!!,
                    nickname = reviewer.nickname,
                    tierId = reviewerTierId,
                )
            )
        )
    }

    private fun notifyReviewReceivedOnConfirm(
        game: Game,
        reviewer: User,
        reviewee: User,
        review: GameResultConfirmCommand.ReviewCommand,
        receiverProfileId: String,
        reviewerTierId: Long,
    ) {
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
            reviewId = savedReview.id!!,
            reviewerNickname = reviewer.nickname,
            reviewerTierId = reviewerTierId,
            gameId = game.id!!,
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
                receiverProfileId = receiverProfileId,
                gameId = game.id!!,
                reviewId = savedReview.id!!,
                reviewer = ReviewReceivedNotificationCreatedPayload.ReviewerSummary(
                    userId = reviewer.id!!,
                    nickname = reviewer.nickname,
                    tierId = reviewerTierId,
                )
            )
        )
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

    private fun applyLpAndTierUpdate(
        winnerProfile: UserSportProfile,
        loserProfile: UserSportProfile,
        sportId: Long,
    ) {
        // 승/패 수 갱신
        winnerProfile.recordWin()
        loserProfile.recordLoss()

        // 각자 이번 경기가 몇 번째 경기인지 계산 (업데이트 후 wins+losses 기준)
        val winnerGameNo = (winnerProfile.wins + winnerProfile.losses)
        val loserGameNo = (loserProfile.wins + loserProfile.losses)

        // 구간별 lp 증감 계산
        val winnerLpDelta = calcWinLpDelta(winnerGameNo)
        val loserLpDelta = calcLoseLpDelta(loserGameNo)

        // lp 반영 (0 미만 방지)
        winnerProfile.lp += winnerLpDelta
        loserProfile.lp = (loserProfile.lp - loserLpDelta).coerceAtLeast(0)

        // lp 기준 tier 재계산/갱신
        winnerProfile.changeTier(resolveTierOrThrow(sportId, winnerProfile.lp))
        loserProfile.changeTier(resolveTierOrThrow(sportId, loserProfile.lp))
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
