package org.appjam.smashing.domain.outbox.components

import org.appjam.smashing.domain.outbox.dto.OutboxPublishEvent
import org.appjam.smashing.domain.outbox.dto.OutboxSendEvent
import org.appjam.smashing.domain.outbox.entity.OutboxEvent
import org.appjam.smashing.domain.outbox.repository.OutboxEventRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OutboxEventRecordListener(
    private val outboxEventRepository: OutboxEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun record(
        event: OutboxPublishEvent
    ) {
        val savedOutboxEvent = outboxEventRepository.save(
            OutboxEvent.create(event.userId, event.eventType, event.payloadJson)
        )

        applicationEventPublisher.publishEvent(
            OutboxSendEvent(requireNotNull(savedOutboxEvent.id))
        )
    }
}
