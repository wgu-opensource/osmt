package edu.wgu.osmt

import edu.wgu.osmt.config.AppConfig
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ActiveProfiles("test,apiserver")
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
@ContextConfiguration(classes = [ServerConnection::class])
@Import(ServerConnection::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class ApplicationTest {

    @Autowired
    protected lateinit var appConfig: AppConfig

    @AfterAll
    fun stopRedis() {
        embeddedRedisServer.stop()
    }

    companion object {
        val embeddedRedisServer = redis.embedded.RedisServer(6380)

        init {
            embeddedRedisServer.start()
        }
    }
}

@TestConfiguration
class ServerConnection {
    @Bean
    @Primary
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val redisStandaloneConfiguration = RedisStandaloneConfiguration("localhost", 6380)
        return LettuceConnectionFactory(redisStandaloneConfiguration)
    }
}

