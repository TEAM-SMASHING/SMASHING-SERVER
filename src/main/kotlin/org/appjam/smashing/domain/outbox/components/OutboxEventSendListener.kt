package org.appjam.smashing.domain.outbox.components

import org.appjam.smashing.domain.outbox.dto.OutboxSendEvent
import org.appjam.smashing.domain.outbox.service.OutboxEventService
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
@Profile("!test")
class OutboxEventSendListener(
    private val outboxEventService: OutboxEventService,
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun send(event: OutboxSendEvent) {
        outboxEventService.sendAsync(event.outboxEventId)
    }
}
