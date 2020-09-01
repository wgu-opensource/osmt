package edu.wgu.osmt.messagequeue

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext

@Configuration
class RedisConfig {
    @Bean
    fun template(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> =
        ReactiveRedisTemplate(factory, RedisSerializationContext.string())
}

