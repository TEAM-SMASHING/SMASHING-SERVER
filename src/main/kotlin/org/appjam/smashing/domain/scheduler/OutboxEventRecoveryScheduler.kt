package org.appjam.smashing.domain.scheduler

import org.appjam.smashing.domain.outbox.service.OutboxEventService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OutboxEventRecoveryScheduler(
    private val outboxEventService: OutboxEventService,
) {

    @Scheduled(fixedDelay = RECOVERY_SCHEDULE_DELAY_MILLIS)
    fun recoverStaleProcessingEvents() {
        runCatching {
            outboxEventService.recoverStaleProcessingEvents()
        }.onFailure { ex ->
            log.error("Outbox stale processing recovery scheduler failed.", ex)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OutboxEventRecoveryScheduler::class.java)
        private const val RECOVERY_SCHEDULE_DELAY_MILLIS = 30_000L
    }
}
