package edu.wgu.osmt

import edu.wgu.osmt.config.AppConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.test.context.ActiveProfiles
import javax.annotation.PreDestroy

@SpringBootTest
@ActiveProfiles("test,apiserver")
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
abstract class ApplicationTest {

    @Autowired
    protected lateinit var appConfig: AppConfig
}

@TestConfiguration
class EmbeddedRedisConfig {
    lateinit var embeddedRedisServer: redis.embedded.RedisServer

    init {
        this.embeddedRedisServer = redis.embedded.RedisServer(6380)
        embeddedRedisServer.start()
    }

    @PreDestroy
    fun stopRedis() {
        embeddedRedisServer.stop()
    }
}
