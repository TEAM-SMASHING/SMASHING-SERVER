package org.appjam.smashing.domain.outbox.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.appjam.smashing.global.auth.security.components.CurrentUserProvider
import org.appjam.smashing.domain.outbox.components.SseEmitterRegistry
import org.appjam.smashing.domain.outbox.service.OutboxEventService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Tag(name = "Sse")
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

        runCatching {
            emitter.send(
                SseEmitter.event()
                    .name(SYSTEM_CONNECTED_EVENT)
                    .data("""{"type":"$SYSTEM_CONNECTED_EVENT"}""", MediaType.APPLICATION_JSON)
            )
        }.onSuccess {
            // 대기중인 이벤트 비동기 전송 시도
            outboxEventService.enqueuePendingSendsAsync(userId)
        }.onFailure {
            sseEmitterRegistry.removeEmitter(userId, emitter)
        }

        return emitter
    }

    companion object {
        private const val SSE_TIMEOUT_MILLIS = 30 * 60 * 1000L
        private const val SYSTEM_CONNECTED_EVENT = "system.connected"
    }
}
