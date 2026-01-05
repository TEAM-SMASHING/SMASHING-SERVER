package org.appjam.smashing.domain.outbox.service

import org.appjam.smashing.domain.outbox.components.SseEmitterRegistry
import org.appjam.smashing.domain.outbox.repository.OutboxEventRepository
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
    fun send(outboxEventId: String) {
        val now = LocalDateTime.now()

        // 선점: PENDING -> PROCESSING
        if (outboxEventRepository.markProcessingIfPending(outboxEventId, now = now) != 1) return

        val event = outboxEventRepository.findByIdOrNull(outboxEventId) ?: run {
            // 선점은 됐는데 row를 못 읽는 비정상 케이스 → 바로 FAILED
            outboxEventRepository.markFailedIfProcessing(outboxEventId, now = now)
            return
        }

        // 연결 없으면 PROCESSING -> PENDING 복구 (재시도 가능하게)
        if (!sseEmitterRegistry.hasConnection(event.userId)) {
            outboxEventRepository.markPendingIfProcessing(outboxEventId, now = now)
            return
        } // TODO: 오류 발생으로 processing에서 고여있는 이벤트들 스케쥴러 도입해서 처리

        // 전송 / 상태 확정
        val success = runCatching {
            sseEmitterRegistry.send(event.userId, event.eventType.eventName, event.payload)
        }.isSuccess

        if (success) event.markAsSent() else event.markAsFailed()

        // 상태 저장
        outboxEventRepository.save(event)
    }
}
