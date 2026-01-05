package org.appjam.smashing.domain.outbox.dto

data class OutboxSendEvent(
    val outboxEventId: String,
)
