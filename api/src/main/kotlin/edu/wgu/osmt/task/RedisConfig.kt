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
import org.springframework.session.data.redis.config.ConfigureRedisAction
import java.net.URI


@Configuration
@EnableRedisHttpSession
class RedisConfig: AbstractHttpSessionApplicationInitializer(){

    @Value("\${spring.redis.url}")
    lateinit var redisUrl: String

    @Bean
    fun configureRedisAction(): ConfigureRedisAction {
        return ConfigureRedisAction.NO_OP
    }

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val parsedUri = URI(redisUrl)
        val redisDb = if (parsedUri.path.isNotEmpty()) parsedUri.path.toInt() else 0

        val redisStandaloneConfiguration = RedisStandaloneConfiguration(parsedUri.host, parsedUri.port)
        redisStandaloneConfiguration.database = redisDb
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

