package org.appjam.smashing.domain.lp.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.appjam.smashing.domain.game.entity.Game
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(
    indexes = [
        Index(name = "idx_lp_history_profile_id", columnList = "user_sport_profile_id"),
        Index(name = "idx_lp_history_game_id", columnList = "game_id"),
    ]
)
@Comment("LP 변동 이력 정보")
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "update lp_history set deleted_at = now() where id = ?")
class LpHistory(
    @Id
    @Tsid
    @Column(length = 13)
    @Comment("LP 이력 IDX")
    val id: String? = null,

    @Column(nullable = false)
    @Comment("변동 LP")
    val deltaLp: Int,

    @Column(nullable = false)
    @Comment("변동 전 LP")
    val beforeLp: Int,

    @Column(nullable = false)
    @Comment("변동 후 LP")
    val afterLp: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_sport_profile_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("유저-스포츠 프로필 IDX")
    val userSportProfile: UserSportProfile,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "game_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("경기 IDX")
    val game: Game,
) : BaseEntity() {

    companion object {
        fun create(
            userSportProfile: UserSportProfile,
            game: Game,
            beforeLp: Int,
            deltaLp: Int,
            afterLp: Int,
        )= LpHistory(
                deltaLp = deltaLp,
                beforeLp = beforeLp,
                afterLp = afterLp,
                userSportProfile = userSportProfile,
                game = game,
            )
        }
}
