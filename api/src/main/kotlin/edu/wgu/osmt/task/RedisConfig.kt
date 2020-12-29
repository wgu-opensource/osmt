package edu.wgu.osmt.task

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer


@Configuration
@EnableRedisHttpSession
class RedisConfig: AbstractHttpSessionApplicationInitializer(){

    @Value("\${redis.uri}")
    lateinit var redisUri: String

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val (redisHost, redisPort) = redisUri.split(":")

        val redisStandaloneConfiguration = RedisStandaloneConfiguration(redisHost, redisPort.toInt())
        return LettuceConnectionFactory(redisStandaloneConfiguration)
    }

    @Bean
    fun redisTaskTemplate(): RedisTemplate<String, Task> {
        val template = RedisTemplate<String, Task>()
        val stringSerializer: RedisSerializer<String> = StringRedisSerializer()
        val jackson2JsonRedisSerializer: Jackson2JsonRedisSerializer<Task> =
            Jackson2JsonRedisSerializer(Task::class.java)
        template.setConnectionFactory(redisConnectionFactory())
        template.keySerializer = stringSerializer
        template.hashKeySerializer = stringSerializer
        template.valueSerializer = jackson2JsonRedisSerializer
        template.hashValueSerializer = jackson2JsonRedisSerializer
        template.setEnableTransactionSupport(false)
        template.afterPropertiesSet()
        return template
    }
}

