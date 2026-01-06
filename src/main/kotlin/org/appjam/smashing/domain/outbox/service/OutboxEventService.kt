package org.appjam.smashing.domain.outbox.service

import org.appjam.smashing.domain.outbox.enums.OutboxEventStatus
import org.appjam.smashing.domain.outbox.repository.OutboxEventRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException

@Service
class OutboxEventService(
    private val outboxEventRepository: OutboxEventRepository,
    private val outboxEventSender: OutboxEventSender,
    @Qualifier("outboxEventExecutor") private val outboxEventExecutor: Executor,
) {

    fun enqueuePendingSendsAsync(
        userId: String
    ) {
        try {
            outboxEventExecutor.execute {
                runCatching {
                    val ids = outboxEventRepository.findIdsByUserIdAndStatus(userId, OutboxEventStatus.PENDING, PageRequest.of(0, 200),)
                    if (ids.isEmpty()) return@runCatching
                    ids.forEach { id ->
                        runCatching { outboxEventSender.send(id) }
                            .onFailure { ex ->
                                log.warn("Outbox send failed in enqueuePendingSendsAsync. outboxEventId={}", id, ex)
                            }
                    }
                }.onFailure { ex -> log.warn("enqueuePendingSendsAsync failed. userId={}", userId, ex)
                }
            }
        } catch (ex: RejectedExecutionException) {
            log.warn("Executor rejected enqueuePendingSendsAsync. userId={}", userId, ex)
        }
    }

    fun sendAsync(
        outboxEventId: String
    ) {
        try {
            outboxEventExecutor.execute {
                runCatching {
                    outboxEventSender.send(outboxEventId)
                }.onFailure { ex ->
                    log.warn("Outbox send failed in executor. outboxEventId={}", outboxEventId, ex)
                }
            }
        } catch (ex: RejectedExecutionException) {
            log.warn("Executor rejected sendAsync. outboxEventId={}", outboxEventId, ex)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OutboxEventService::class.java)
    }
}
