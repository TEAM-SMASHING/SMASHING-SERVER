package org.appjam.smashing.domain.outbox.components

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

@Component
class SseEmitterRegistry {

    private val emitters = ConcurrentHashMap<String, ConcurrentLinkedQueue<SseEmitter>>()

    /*
     * userId에 emitter 추가
     */
    fun add(
        userId: String,
        emitter: SseEmitter
    ) {
        emitters.computeIfAbsent(userId) {
            ConcurrentLinkedQueue()
        }.add(emitter)

        // emitter가 종료/타임아웃/에러 났을 때 자동으로 remove
        bindLifecycle(userId, emitter)
    }

    /*
     * 해당 userId에 연결된 emitter가 하나라도 있는지 확인
     */
    fun hasConnection(userId: String): Boolean = emitters[userId]?.peek() != null

    /**
     * userId에 연결된 emitter들로 전송을 시도하고,
     * 하나라도 성공하면 true, 전부 실패/없으면 false
     */
    fun send(
        userId: String,
        eventName: String,
        payloadJson: String
    ): Boolean {
        // userId에 연결된 emitter 큐 가져오기
        val queue = emitters[userId] ?: return false

        val event = SseEmitter.event()
            .name(eventName)
            .data(payloadJson, MediaType.APPLICATION_JSON)

        // 최소 한 번이라도 전송 성공했는지 여부
        var sentAtLeastOnce = false

        val snapshot = queue.toList()

        for (emitter in snapshot) {
            runCatching {
                emitter.send(event)
                sentAtLeastOnce = true
            }.onFailure {
                remove(userId, emitter)
            }
        }

        return sentAtLeastOnce
    }

    /*
     * userId에 emitter 제거
     */
    fun removeEmitter(
        userId: String,
        emitter: SseEmitter
    ) {
        remove(userId, emitter)
    }

    /*
     * emitter의 종료/타임아웃/에러 시 registry에서 제거
     */
    private fun bindLifecycle(
        userId: String,
        emitter: SseEmitter
    ) {
        emitter.apply {
            val cleanup = { remove(userId, this) }

            onCompletion(cleanup)
            onTimeout(cleanup)
            onError { cleanup() }
        }
    }

    /*
     * userId에 emitter 제거
     * - emitter 큐가 비어있으면 userId 키도 함께 제거
     */
    private fun remove(userId: String, emitter: SseEmitter) {
        emitters.computeIfPresent(userId) { _, queue ->
            queue.remove(emitter)
            queue.takeIf { it.peek() != null } // it 사용하도록 깔끔
        }
    }
}
