package org.appjam.smashing.domain.outbox.components

import com.fasterxml.jackson.databind.ObjectMapper
import org.appjam.smashing.domain.outbox.dto.OutboxPublishEvent
import org.appjam.smashing.domain.outbox.dto.SsePayload
import org.appjam.smashing.domain.outbox.enums.SseEventType
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class OutboxEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) {

    fun publish(
        userId: String,
        eventType: SseEventType,
        payload: SsePayload
    ) {
        val payloadJson = objectMapper.writeValueAsString(payload)

        applicationEventPublisher.publishEvent(
            OutboxPublishEvent(userId, eventType, payloadJson)
        )
    }
}
