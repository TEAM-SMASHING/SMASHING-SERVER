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

    /*
     * userId에 연결된 모든 emitter에 이벤트 전송
     */
    fun send(
        userId: String,
        eventName: String,
        payloadJson: String
    ) {
        // 해당 userId에 연결된 emitter가 없으면 종료
        val queue = emitters[userId] ?: return

        // 이벤트 생성
        val event = SseEmitter.event()
            .name(eventName)
            .data(payloadJson, MediaType.APPLICATION_JSON)

        // 유저의 모든 emitter에 이벤트 전송
        for (emitter in queue) {
            val isSucceed = runCatching { emitter.send(event) }.isSuccess
            if (!isSucceed) remove(userId, emitter)
        }
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
        val cleanup = { remove(userId, emitter) }

        emitter.onCompletion(cleanup)
        emitter.onTimeout(cleanup)
        emitter.onError { cleanup() }
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
