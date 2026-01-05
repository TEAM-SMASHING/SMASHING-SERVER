package org.appjam.smashing.domain.outbox.dto

import org.appjam.smashing.domain.outbox.enums.SseEventType

data class OutboxPublishEvent(
    val userId: String,
    val eventType: SseEventType,
    val payloadJson: String,
)
