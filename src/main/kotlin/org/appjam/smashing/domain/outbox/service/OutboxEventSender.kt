package org.appjam.smashing.domain.outbox.service

import org.appjam.smashing.domain.outbox.components.SseEmitterRegistry
import org.appjam.smashing.domain.outbox.repository.OutboxEventRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class OutboxEventSender(
    private val outboxEventRepository: OutboxEventRepository,
    private val sseEmitterRegistry: SseEmitterRegistry,
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun send(
        outboxEventId: String
    ) {
        val now = LocalDateTime.now()

        // 선점: PENDING -> PROCESSING (성공한 1명만 전송 진행)
        if (outboxEventRepository.markProcessingIfPending(outboxEventId, now = now) != 1) return

        val event = outboxEventRepository.findByIdOrNull(outboxEventId) ?: run {
            // 선점은 됐는데 row를 못 읽는 비정상 케이스 → PROCESSING -> FAILED로 즉시 전환
            val updatedEvent = outboxEventRepository.markFailedIfProcessing(outboxEventId, now = now)

            if (updatedEvent != 1) {
                log.warn("markFailedIfProcessing affected 0 rows. outboxEventId={}", outboxEventId)
            }
            return
        }

        // 연결 없으면 PROCESSING -> PENDING 복구 (재시도 가능하게)
        if (!sseEmitterRegistry.hasConnection(event.userId)) {
            val updatedEvent = outboxEventRepository.markPendingIfProcessing(outboxEventId, now = now)

            if (updatedEvent != 1) {
                log.warn("markPendingIfProcessing affected 0 rows. outboxEventId={}", outboxEventId)
            }
            return
        } // TODO: 오류 발생으로 processing에서 고여있는 이벤트들 스케쥴러 도입해서 처리

        // 전송 시도
        val deliveredResult = runCatching {
            sseEmitterRegistry.send(event.userId, event.eventType.eventName, event.payload)
        }.getOrElse { ex ->
            log.warn("SSE registry send threw. outboxEventId={}, userId={}, eventType={}", outboxEventId, event.userId, event.eventType, ex)
            false
        }

        // 결과에 따른 상태 확정
        if (deliveredResult) {
            event.markAsSent()
        } else {
            log.warn("SSE not delivered. outboxEventId={}, userId={}, eventType={}", outboxEventId, event.userId, event.eventType)
            event.markAsFailed()
        }

        // 상태 저장 (DB 반영)
        runCatching { outboxEventRepository.save(event) }
            .onFailure { ex -> log.error("OutboxEvent save failed. outboxEventId={}", outboxEventId, ex) }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OutboxEventSender::class.java)
    }
}
