package org.appjam.smashing.domain.outbox.service

import org.appjam.smashing.domain.outbox.enums.OutboxEventStatus
import org.appjam.smashing.domain.outbox.repository.OutboxEventRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
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
                    val ids = outboxEventRepository.findIdsByUserIdAndStatus(
                        userId = userId,
                        status = OutboxEventStatus.PENDING,
                        pageable = PageRequest.of(0, PENDING_SEND_BATCH_SIZE),
                    )

                    if (ids.isEmpty()) return@runCatching

                    ids.forEach { id ->
                        runCatching { outboxEventSender.send(id) }
                            .onFailure { ex ->
                                log.warn("Outbox send failed in enqueuePendingSendsAsync. outboxEventId={}", id, ex)
                            }
                    }
                }.onFailure { ex ->
                    log.warn("enqueuePendingSendsAsync failed. userId={}", userId, ex)
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

    @Transactional
    fun recoverStaleProcessingEvents() {
        val now = LocalDateTime.now()
        val threshold = now.minusSeconds(STALE_PROCESSING_SECONDS)

        val staleEvents = outboxEventRepository.findStaleProcessingEvents(
            threshold = threshold,
            pageable = PageRequest.of(0, STALE_PROCESSING_BATCH_SIZE),
        )

        if (staleEvents.isEmpty()) return

        staleEvents.forEach { event ->
            val eventId = event.id
            if (eventId == null) {
                log.warn("Stale processing event id is null. userId={}, retryCount={}", event.userId, event.retryCount)
                return@forEach
            }

            runCatching {
                if (event.retryCount >= MAX_RETRY) {
                    val updated = outboxEventRepository.markFailedIfProcessing(
                        id = eventId,
                        now = now,
                    )

                    if (updated == 1) {
                        log.warn(
                            "Recovered stale PROCESSING event to FAILED. outboxEventId={}, userId={}, retryCount={}",
                            eventId, event.userId, event.retryCount
                        )
                    }
                } else {
                    val updated = outboxEventRepository.markPendingIfProcessing(
                        id = eventId,
                        now = now,
                    )

                    if (updated == 1) {
                        log.warn(
                            "Recovered stale PROCESSING event to PENDING. outboxEventId={}, userId={}, retryCount={}",
                            eventId, event.userId, event.retryCount
                        )
                    }
                }
            }.onFailure { ex ->
                log.error("Failed to recover stale PROCESSING event. outboxEventId={}", eventId, ex)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OutboxEventService::class.java)

        private const val MAX_RETRY = 3
        private const val PENDING_SEND_BATCH_SIZE = 200
        private const val STALE_PROCESSING_SECONDS = 60L
        private const val STALE_PROCESSING_BATCH_SIZE = 100
    }
}
