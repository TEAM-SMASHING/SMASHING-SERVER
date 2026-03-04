package org.appjam.smashing.global.config

import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    private val redisProperties: RedisProperties
) {
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration(
            redisProperties.host,
            redisProperties.port
        )

        redisProperties.password?.let {
            config.setPassword(it)
        }

        return LettuceConnectionFactory(config)
    }

    @Bean
    fun stringRedisTemplate(
        connectionFactory: RedisConnectionFactory
    ) = StringRedisTemplate(connectionFactory).apply {
        keySerializer = StringRedisSerializer()
        valueSerializer = StringRedisSerializer()
    }
}
