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
import jakarta.persistence.Table
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.appjam.smashing.domain.game.enums.GameSubmissionRejectReason
import org.appjam.smashing.domain.game.enums.GameSubmissionStatus
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@Table(
    indexes = [
        Index(name = "idx_grs_game_id", columnList = "game_id"),
        Index(name = "idx_grs_submitter_profile_id", columnList = "submitter_profile_id"),
        Index(name = "idx_grs_confirmer_profile_id", columnList = "confirmer_profile_id"),
        Index(name = "idx_grs_winner_profile_id", columnList = "winner_profile_id"),
        Index(name = "idx_grs_loser_profile_id", columnList = "loser_profile_id"),
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

    @Column
    @Comment("상대가 맞음/틀림 누른 시각")
    var actedAt: LocalDateTime? = null,

    @Column(nullable = false)
    @Comment("제출 회차")
    val attemptNo: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    @Comment("제출 상태")
    var status: GameSubmissionStatus = GameSubmissionStatus.SUBMITTED,

    @Enumerated(EnumType.STRING)
    @Column(name = "reject_reason", columnDefinition = "VARCHAR(30)")
    @Comment("거절 사유")
    var gameSubmissionRejectReason: GameSubmissionRejectReason? = null,

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
        name = "submitter_profile_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("결과 제출자 유저-스포츠 프로필 IDX")
    val submitterProfile: UserSportProfile,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "confirmer_profile_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("결과 확인자 유저-스포츠 프로필 IDX")
    val confirmerProfile: UserSportProfile,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "winner_profile_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("승자 유저-스포츠 프로필 IDX(제출안 기준)")
    val winnerProfile: UserSportProfile,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "loser_profile_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("패자 유저-스포츠 프로필 IDX(제출안 기준)")
    val loserProfile: UserSportProfile,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "submitter_user_id",
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("결과 제출자 IDX")
    val submitter: User? = null, // TODO: Submission 리팩토링 완료 후 제거 예정
) : BaseEntity() {

    companion object {
        fun create(
            game: Game,
            submitterProfile: UserSportProfile,
            confirmerProfile: UserSportProfile,
            winnerProfile: UserSportProfile,
            loserProfile: UserSportProfile,
            attemptNo: Int,
        ) = GameResultSubmission(
            attemptNo = attemptNo,
            game = game,
            submitterProfile = submitterProfile,
            confirmerProfile = confirmerProfile,
            winnerProfile = winnerProfile,
            loserProfile = loserProfile,
            submitter = submitterProfile.user,
        )
    }

    fun accept(
        actedAt: LocalDateTime
    ) {
        status = GameSubmissionStatus.ACCEPTED
        this.actedAt = actedAt
    }

    fun reject(
        actedAt: LocalDateTime,
    ) {
        status = GameSubmissionStatus.REJECTED
        this.actedAt = actedAt
    }

    fun rejectWithReason(
        reason: GameSubmissionRejectReason,
        actedAt: LocalDateTime,
    ) {
        status = GameSubmissionStatus.REJECTED
        gameSubmissionRejectReason = reason
        this.actedAt = actedAt
    }
}
