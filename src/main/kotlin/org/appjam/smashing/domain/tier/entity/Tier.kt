package org.appjam.smashing.domain.tier.entity

import jakarta.persistence.Column
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.appjam.smashing.domain.sport.entity.Sport
import org.hibernate.annotations.Comment

@Entity
@Table(
    indexes = [
        Index(name = "idx_tier_sport_id", columnList = "sport_id"),
    ]
)
@Comment("티어 정보")
class Tier(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("티어 IDX")
    val id: Long? = null,

    @Column(nullable = false, length = 50)
    @Comment("티어명")
    val name: String,

    @Column(nullable = false)
    @Comment("정렬 순서(높을수록 상위 티어)")
    val orderNo: Int,

    @Column(nullable = false)
    @Comment("최소 LP")
    val minLp: Int,

    @Column(nullable = false)
    @Comment("최대 LP")
    val maxLp: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "sport_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("스포츠 IDX")
    val sport: Sport,
)
