package org.appjam.smashing.domain.user.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.*
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.hibernate.annotations.Comment

@Entity
@Table(
    name = "block"
)
@Comment("차단 정보")
class Block(
    @Id
    @Tsid
    @Column(length = 13)
    @Comment("차단 IDX")
    val id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "blocker_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("차단한 사람")
    val blocker: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "blocked_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("차단당한 사람")
    val blockedUser: User,
) : BaseEntity() {
    companion object {
        fun create(
            blocker: User,
            blockedUser: User
        ) = Block(
            blocker = blocker,
            blockedUser = blockedUser,
        )
    }
}
