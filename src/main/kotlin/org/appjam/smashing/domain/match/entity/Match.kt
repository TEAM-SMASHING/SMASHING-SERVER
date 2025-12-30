package org.appjam.smashing.domain.match.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.*
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.appjam.smashing.domain.match.enums.MatchStatus
import org.appjam.smashing.domain.sport.entity.Sport
import org.appjam.smashing.domain.user.entity.User
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@Table(
    indexes = [
        Index(name = "idx_match_requester_user_id", columnList = "requester_user_id"),
        Index(name = "idx_match_receiver_user_id", columnList = "receiver_user_id"),
        Index(name = "idx_match_sport_id", columnList = "sport_id"),
    ]
)
@Comment("매칭 정보")
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "update match set deleted_at = now() where id = ?")
class Match(
    @Id
    @Tsid
    @Column(length = 13)
    @Comment("매치 IDX")
    val id: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(30)")
    @Comment("매칭 상태")
    val status: MatchStatus,

    @Column
    @Comment("응답 시각")
    val respondedAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "requester_user_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("발신자 유저 IDX")
    val requester: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "receiver_user_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("수신자 유저 IDX")
    val receiver: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "sport_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("스포츠 IDX")
    val sport: Sport,
) : BaseEntity()
