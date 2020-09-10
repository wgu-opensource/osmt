package edu.wgu.osmt

import edu.wgu.osmt.config.AppConfig
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.BeforeAll
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
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import javax.annotation.PreDestroy

@SpringBootTest
@ActiveProfiles("test")
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = [RedisTestConnection::class])
@Import(RedisTestConnection::class)
abstract class ApplicationTest {

    @Autowired
    protected lateinit var appConfig: AppConfig

    @PreDestroy
    fun tearDown() {
        redisContainer.stop()
    }

    companion object {

        @Container
        val redisContainer: KGenericContainer = KGenericContainer("redis:6.0.6").apply {
            withExposedPorts(6379)
            start()
        }

        @Container
        val mysqlContainer: KGenericContainer = KGenericContainer("mysql:8").apply {
            withExposedPorts(3306)
            withEnv(mutableMapOf("MYSQL_ROOT_PASSWORD" to "password"))
            start()
        }
    }
}

@TestConfiguration
class RedisTestConnection {
    @Bean
    @Primary
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val host = ApplicationTest.redisContainer.host
        val port = ApplicationTest.redisContainer.firstMappedPort
        println(host)
        println(port)
        val redisStandaloneConfiguration = RedisStandaloneConfiguration(host, port)
        return LettuceConnectionFactory(redisStandaloneConfiguration)
    }

    @Bean
    @Primary
    fun db(): Database = Database.connect(
        url = "jdbc:mysql://root:password@${ApplicationTest.mysqlContainer.host}:${ApplicationTest.mysqlContainer.firstMappedPort}",
        driver = "com.mysql.cj.jdbc.Driver", user = "", password = ""
    )
}

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)
