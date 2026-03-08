package org.appjam.smashing.domain.notification.entity

import jakarta.persistence.*
import org.appjam.smashing.domain.notification.enums.NotificationType
import org.hibernate.annotations.Comment

@Entity
@Comment("알림 템플릿 정보")
class NotificationTemplate( // TODO: 알림 전체 리팩토링 후 엔티티 삭제 예정
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("템플릿 IDX")
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Comment("알림 타입")
    val type: NotificationType,

    @Column(nullable = false)
    @Comment("알림 제목")
    val title: String,

    @Column(nullable = false, length = 500)
    @Comment("알림 내용 템플릿")
    val content: String,
)
