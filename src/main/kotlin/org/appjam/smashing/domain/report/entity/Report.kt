package org.appjam.smashing.domain.report.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.*
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.appjam.smashing.domain.report.enums.ReportType
import org.appjam.smashing.domain.user.entity.User
import org.hibernate.annotations.Comment

@Entity
@Table(
    name = "report",
)
@Comment("신고 정보")
class Report(
    @Id
    @Tsid
    @Column(length = 13)
    @Comment("신고 IDX")
    val id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("신고자")
    val reporter: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("피신고자")
    val reportedUser: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(30)")
    @Comment("신고 타입")
    val reportType: ReportType,

    @Column(nullable = false, length = 100)
    @Comment("기타 사유")
    val reasonDetail: String? = null
) : BaseEntity() {

    companion object {

    }
}
