package org.appjam.smashing.domain.outbox.service

import org.appjam.smashing.domain.outbox.enums.OutboxEventStatus
import org.appjam.smashing.domain.outbox.repository.OutboxEventRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.concurrent.Executor

@Service
class OutboxEventService(
    private val outboxEventRepository: OutboxEventRepository,
    private val outboxEventSender: OutboxEventSender,
    @Qualifier("outboxEventExecutor") private val outboxEventExecutor: Executor,
) {
    fun enqueuePendingSends(
        userId: String
    ) {
        val ids = outboxEventRepository.findPendingIds(
            userId = userId,
            status = OutboxEventStatus.PENDING,
            pageable = PageRequest.of(0, 200),
        )
        if (ids.isEmpty()) return

        ids.forEach { id ->
            outboxEventExecutor.execute { outboxEventSender.send(id) }
        }
    }

    fun sendAsync(
        outboxEventId: String
    ) {
        outboxEventExecutor.execute {
            outboxEventSender.send(outboxEventId)
        }
    }
}
