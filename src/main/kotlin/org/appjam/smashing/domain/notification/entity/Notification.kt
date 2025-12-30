package org.appjam.smashing.domain.notification.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.*
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.appjam.smashing.domain.user.entity.User
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(
    indexes = [
        Index(name = "idx_notification_user_id", columnList = "user_id"),
        Index(name = "idx_notification_template_id", columnList = "notification_template_id"),
    ]
)
@Comment("알림 정보")
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "update notification set deleted_at = now() where id = ?")
class Notification(
    @Id
    @Tsid
    @Column(length = 13)
    @Comment("알림 IDX")
    val id: String? = null,

    @Column(nullable = false, length = 500)
    @Comment("알림 치환 파라미터(JSON)")
    val params: String,

    @Column(nullable = false)
    @Comment("알림 읽음 여부")
    var isRead: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("수신자 유저 IDX")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "notification_template_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("템플릿 IDX")
    val notificationTemplate: NotificationTemplate,
) : BaseEntity()
