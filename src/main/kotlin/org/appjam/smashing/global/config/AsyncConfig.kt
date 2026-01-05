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

        executor.corePoolSize = 4
        executor.maxPoolSize = 8
        executor.queueCapacity = 500
        executor.keepAliveSeconds = 30
        executor.setThreadNamePrefix("Outbox-Sse-")
        executor.initialize()

        return executor
    }

    companion object {
        const val OUTBOX_EVENT_EXECUTOR = "outboxEventExecutor"
    }
}
