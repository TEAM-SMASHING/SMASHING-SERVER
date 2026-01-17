package org.appjam.smashing.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
class AsyncConfig {

    @Bean(name = [OUTBOX_EVENT_EXECUTOR])
    fun outboxEventExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()

        executor.corePoolSize = 2
        executor.maxPoolSize = 4
        executor.queueCapacity = 200
        executor.keepAliveSeconds = 30
        executor.setThreadNamePrefix("Outbox-Sse-")
        executor.initialize()

        return executor
    }

    companion object {
        private const val OUTBOX_EVENT_EXECUTOR = "outboxEventExecutor"
    }
}
