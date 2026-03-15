package org.appjam.smashing.domain.user.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.*
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.appjam.smashing.domain.sport.entity.Sport
import org.appjam.smashing.domain.tier.entity.Tier
import org.hibernate.annotations.*

@Entity
@Table(
    name = "user_sport_profile",
    indexes = [
        Index(name = "idx_usp_user_id", columnList = "user_id"),
        Index(name = "idx_usp_sport_id", columnList = "sport_id"),
        Index(name = "idx_usp_tier_id", columnList = "tier_id"),
    ]
)
@Comment("유저-스포츠 프로필 정보")
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "update user_sport_profile set deleted_at = now() where id = ?")
class UserSportProfile(
    @Id
    @Tsid
    @Column(length = 13)
    @Comment("유저-스포츠 프로필 IDX")
    val id: String? = null,

    @Column(nullable = false)
    @Comment("현재 LP")
    var lp: Int = 0,

    @Column(nullable = false)
    @Comment("누적 승리 수")
    var wins: Int = 0,

    @Column(nullable = false)
    @Comment("누적 패배 수")
    var losses: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("유저 IDX")
    val user: User,

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
        name = "tier_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("현재 티어 IDX")
    var tier: Tier,
) : BaseEntity() {

    fun changeTier(
        newTier: Tier
    ) {
        tier = newTier
    }

    fun recordWin() {
        wins += 1
    }

    fun recordLoss() {
        losses += 1
    }

    companion object {
        fun create(
            lp: Int,
            user: User,
            sport: Sport,
            tier: Tier,
        ): UserSportProfile = UserSportProfile(
            lp = lp,
            user = user,
            sport = sport,
            tier = tier,
        )
    }
}
