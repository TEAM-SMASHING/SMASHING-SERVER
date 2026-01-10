package org.appjam.smashing.domain.game.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.*
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.appjam.smashing.domain.game.enums.GameResultRejectReason
import org.appjam.smashing.domain.game.enums.SubmissionStatus
import org.appjam.smashing.domain.user.entity.User
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@Table(
    indexes = [
        Index(name = "idx_grs_game_id", columnList = "game_id"),
        Index(name = "idx_grs_submitter_user_id", columnList = "submitter_user_id"),
        Index(name = "idx_grs_confirmer_user_id", columnList = "confirmer_user_id"),
        Index(name = "idx_grs_winner_user_id", columnList = "winner_user_id"),
        Index(name = "idx_grs_loser_user_id", columnList = "loser_user_id"),
    ]
)
@Comment("경기 결과 제출 정보")
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "update game_result_submission set deleted_at = now() where id = ?")
class GameResultSubmission(
    @Id
    @Tsid
    @Column(length = 13)
    @Comment("결과 제출 IDX")
    val id: String? = null,

    @Column(nullable = false)
    @Comment("제출자 점수")
    val scoreSubmitter: Int,

    @Column(nullable = false)
    @Comment("상대 점수")
    val scoreConfirmer: Int,

    @Column
    @Comment("상대가 맞음/틀림 누른 시각")
    var actedAt: LocalDateTime? = null,

    @Column(nullable = false)
    @Comment("제출 회차")
    val attemptNo: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    @Comment("제출 상태")
    var status: SubmissionStatus = SubmissionStatus.SUBMITTED,

    @Enumerated(EnumType.STRING)
    @Column(name = "reject_reason", columnDefinition = "VARCHAR(30)")
    @Comment("거절 사유")
    var gameResultRejectReason: GameResultRejectReason? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "game_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("경기 IDX")
    val game: Game,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "submitter_user_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("결과 제출자 IDX")
    val submitter: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "confirmer_user_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("결과 확인자 IDX")
    val confirmer: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "winner_user_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("승자 유저 IDX(제출안 기준)")
    val winner: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "loser_user_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("패자 유저 IDX(제출안 기준)")
    val loser: User,
) : BaseEntity() {

    companion object {
        fun create(
            game: Game,
            submitter: User,
            confirmer: User,
            winner: User,
            loser: User,
            attemptNo: Int,
            scoreSubmitter: Int,
            scoreConfirmer: Int,
        ) = GameResultSubmission(
            scoreSubmitter = scoreSubmitter,
            scoreConfirmer = scoreConfirmer,
            attemptNo = attemptNo,
            game = game,
            submitter = submitter,
            confirmer = confirmer,
            winner = winner,
            loser = loser,
        )
    }

    fun accept(
        actedAt: LocalDateTime
    ) {
        status = SubmissionStatus.ACCEPTED
        this.actedAt = actedAt
    }

    fun reject(
        reason: GameResultRejectReason,
        actedAt: LocalDateTime,
    ) {
        status = SubmissionStatus.REJECTED
        gameResultRejectReason = reason
        this.actedAt = actedAt
    }
}
