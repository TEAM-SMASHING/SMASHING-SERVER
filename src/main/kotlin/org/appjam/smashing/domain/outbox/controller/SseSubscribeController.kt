package org.appjam.smashing.domain.outbox.controller

import org.appjam.smashing.domain.outbox.components.CurrentUserProvider
import org.appjam.smashing.domain.outbox.components.SseEmitterRegistry
import org.appjam.smashing.domain.outbox.service.OutboxEventService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/v1/sse")
class SseSubscribeController(
    private val sseEmitterRegistry: SseEmitterRegistry,
    private val currentUserProvider: CurrentUserProvider,
    private val outboxEventService: OutboxEventService,
) {
    /*
     * SSE 구독 API
     */
    @GetMapping("/subscribe", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(): SseEmitter {
        val userId = currentUserProvider.currentUserId()

        val emitter = SseEmitter(SSE_TIMEOUT_MILLIS)

        // registry에 emitter 추가
        sseEmitterRegistry.add(userId, emitter)

        val isConnected = runCatching {
            emitter.send(
                SseEmitter.event()
                    .name(SYSTEM_CONNECTED_EVENT)
                    .data("""{"type":"$SYSTEM_CONNECTED_EVENT"}""", MediaType.APPLICATION_JSON)
            )
        }.onFailure {
            sseEmitterRegistry.removeEmitter(userId, emitter)
        }.isSuccess

        // 연결이 정상인 경우 backlog flush
        if (isConnected) {
            outboxEventService.enqueuePendingSendsAsync(userId)
        }

        return emitter
    }

    companion object {
        private const val SSE_TIMEOUT_MILLIS = 30 * 60 * 1000L
        private const val SYSTEM_CONNECTED_EVENT = "system.connected"
    }
}
