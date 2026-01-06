package org.appjam.smashing.domain.outbox.components

import com.fasterxml.jackson.databind.ObjectMapper
import org.appjam.smashing.domain.outbox.dto.OutboxPublishEvent
import org.appjam.smashing.domain.outbox.dto.SsePayload
import org.appjam.smashing.domain.outbox.enums.SseEventType
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class OutboxEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) {

    fun publish(userId: String, eventType: SseEventType, payload: SsePayload) {
        if (userId.isBlank()) {
            log.warn("Outbox publish skipped: blank userId. eventType={}", eventType)
            return
        }

        val payloadJson = runCatching { objectMapper.writeValueAsString(payload) }
            .getOrElse { ex -> log.warn("Outbox publish skipped: serialization failed. userId={}, eventType={}, payloadClass={}",
                userId, eventType, payload::class.qualifiedName ?: payload::class.java.name, ex)
                return
            }

        applicationEventPublisher.publishEvent(
            OutboxPublishEvent(userId, eventType, payloadJson)
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(OutboxEventPublisher::class.java)
    }
}
