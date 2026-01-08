package org.appjam.smashing.domain.outbox.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.*
import org.appjam.smashing.domain.common.entity.BaseCreatedEntity
import org.appjam.smashing.domain.outbox.enums.OutboxEventStatus
import org.appjam.smashing.domain.outbox.enums.SseEventType
import org.hibernate.annotations.Comment
import java.time.LocalDateTime

@Entity
@Table(
    indexes = [
        Index(name = "idx_outbox_user_status_id", columnList = "user_id,status,id"),
    ]
)
@Comment("Outbox Event 정보")
class OutboxEvent(

    @Id
    @Tsid
    @Column(length = 13)
    @Comment("Outbox Event IDX")
    val id: String? = null,

    @Column(nullable = false, length = 13)
    @Comment("수신 유저 IDX")
    val userId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    @Comment("Outbox Event 타입")
    val eventType: SseEventType,

    @Column(nullable = false, columnDefinition = "TEXT")
    @Comment("Outbox Event Payload(JSON 문자열)")
    val payload: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("Outbox Event 상태")
    var status: OutboxEventStatus = OutboxEventStatus.PENDING,

    @Column(nullable = false)
    @Comment("전송 시도 횟수")
    var retryCount: Int = 0,

    @Column
    @Comment("전송 성공 시각")
    var sentAt: LocalDateTime? = null,

    @Column
    @Comment("마지막 전송 시도 시각")
    var lastAttemptAt: LocalDateTime? = null,
) : BaseCreatedEntity() {

    companion object {
        private const val MAX_RETRY = 3

        fun create(
            userId: String,
            eventType: SseEventType,
            payload: String
        ) = OutboxEvent(
            userId = userId,
            eventType = eventType,
            payload = payload
        )
    }

    /**
     * 처리 선점(진행중)
     */
    fun markAsProcessing() {
        status = OutboxEventStatus.PROCESSING
        lastAttemptAt = LocalDateTime.now()
    }

    /**
     * 전송 성공
     */
    fun markAsSent() {
        val now = LocalDateTime.now()
        status = OutboxEventStatus.SENT
        lastAttemptAt = now
        sentAt = now
    }

    /**
     * 전송 실패
     * - retryCount 증가
     * - MAX_RETRY 이상이면 FAILED
     * - 그 전이면 PENDING으로 돌려 재시도
     */
    fun markAsFailed() {
        retryCount += 1
        lastAttemptAt = LocalDateTime.now()
        status = if (retryCount >= MAX_RETRY) OutboxEventStatus.FAILED else OutboxEventStatus.PENDING
    }
}
