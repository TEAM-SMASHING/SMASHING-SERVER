package org.appjam.smashing.domain.outbox.components

import org.appjam.smashing.domain.outbox.dto.OutboxPublishEvent
import org.appjam.smashing.domain.outbox.entity.OutboxEvent
import org.appjam.smashing.domain.outbox.repository.OutboxEventRepository
import org.appjam.smashing.domain.outbox.service.OutboxEventService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Component
class OutboxEventRecordListener(
    private val outboxEventRepository: OutboxEventRepository,
    private val outboxEventService: OutboxEventService,
) {
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun record(
        event: OutboxPublishEvent
    ) {
        val savedOutboxEvent = outboxEventRepository.save(
            OutboxEvent.create(event.userId, event.eventType, event.payloadJson)
        )

        val outboxEventId = savedOutboxEvent.id
        if (outboxEventId == null) {
            log.warn("Outbox record saved but id is null. userId={}, eventType={}", event.userId, event.eventType)
            return
        }

        // afterCommit에 비동기로 전송 예약
        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    outboxEventService.sendAsync(outboxEventId)
                }
            }
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(OutboxEventRecordListener::class.java)
    }
}
