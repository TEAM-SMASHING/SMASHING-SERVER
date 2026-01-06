package org.appjam.smashing.domain.outbox.enums

enum class OutboxEventStatus {
    PENDING,
    PROCESSING,
    SENT,
    FAILED
}
