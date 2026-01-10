package org.appjam.smashing.domain.game.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.appjam.smashing.domain.matching.entity.Matching
import org.appjam.smashing.domain.game.enums.GameResultStatus
import org.appjam.smashing.domain.sport.entity.Sport
import org.appjam.smashing.domain.user.entity.User
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "uk_game_matching_id", columnNames = ["matching_id"])
    ],
    indexes = [
        Index(name = "idx_game_matching_id", columnList = "matching_id"),
        Index(name = "idx_game_sport_id", columnList = "sport_id"),
        Index(name = "idx_game_winner_user_id", columnList = "winner_user_id"),
        Index(name = "idx_game_loser_user_id", columnList = "loser_user_id"),
        Index(name = "idx_game_confirmed_submission_id", columnList = "confirmed_submission_id"),
    ]
)
@Comment("경기 정보")
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "update game set deleted_at = now() where id = ?")
class Game(
    @Id
    @Tsid
    @Column(length = 13)
    @Comment("경기 IDX")
    val id: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(30)")
    @Comment("경기 결과 처리 상태")
    var resultStatus: GameResultStatus,

    @Column
    @Comment("승자 점수(확정)")
    var scoreWinner: Int? = null,

    @Column
    @Comment("패자 점수(확정)")
    var scoreLoser: Int? = null,

    @Column
    @Comment("결과 확정 시각")
    var confirmedAt: LocalDateTime? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "matching_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("매칭 IDX")
    val matching: Matching,

    @Column(name = "confirmed_submission_id", length = 13)
    @Comment("최종 확정된 결과 제출 IDX")
    var confirmedSubmissionId: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "sport_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("스포츠 IDX")
    val sport: Sport,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "winner_user_id",
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("승자 유저 IDX(확정 시)")
    var winner: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "loser_user_id",
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("패자 유저 IDX(확정 시)")
    var loser: User? = null,
) : BaseEntity() {

    companion object {
        fun createFromMatching(
            matching: Matching
        ) = Game(
            resultStatus = GameResultStatus.PENDING_RESULT,
            matching = matching,
            sport = matching.sport,
        )
    }

    fun markWaitingConfirmation() {
        resultStatus = GameResultStatus.WAITING_CONFIRMATION
    }

    fun markRejected() {
        resultStatus = GameResultStatus.RESULT_REJECTED
    }

    fun confirmResult(
        submissionId: String,
        winner: User,
        loser: User,
        scoreWinner: Int,
        scoreLoser: Int,
        confirmedAt: LocalDateTime,
    ) {
        resultStatus = GameResultStatus.RESULT_CONFIRMED
        confirmedSubmissionId = submissionId
        this.winner = winner
        this.loser = loser
        this.scoreWinner = scoreWinner
        this.scoreLoser = scoreLoser
        this.confirmedAt = confirmedAt
    }
}
