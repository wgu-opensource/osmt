package edu.wgu.osmt

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.MountableFile
import java.io.File

/**
 * Interface for unit tests that provides dockerized servers
 */
interface BaseDockerizedTest {
    companion object {
        @DynamicPropertySource
        @JvmStatic
        fun dynamicProperties(registry: DynamicPropertyRegistry) {
            val redisPort = Containers.redisContainer.getMappedPort(6379)
            val elasticPort = Containers.elasticContainer.getMappedPort(9200)
            val mySqlPort = Containers.mysqlContainer.getMappedPort(3306)
            println("Redis port")
            registry.add(
                "db.uri",
                { "root:password@${Containers.mysqlContainer.host}:$mySqlPort" },
            )
            registry.add(
                "redis.uri",
                { "${Containers.redisContainer.host}:$redisPort" },
            )
            registry.add(
                "es.uri",
                { "${Containers.elasticContainer.host}:$elasticPort" },
            )
        }
    }
}

object Containers {
    val elasticContainer =
        GenericContainer<Nothing>("docker.elastic.co/elasticsearch/elasticsearch:8.11.3").apply {
            withExposedPorts(9200, 9300)
            withEnv("discovery.type", "single-node")
            withEnv("xpack.security.enabled", "false")
            withEnv("ES_JAVA_OPTS", "-Xms1g -Xmx1g")
            withCreateContainerCmdModifier { cmd ->
                cmd.withMemory(2L * 1024 * 1024 * 1024)
            }
            start()
            println("Elasticsearch port: ${getMappedPort(9200)}")
        }

    val redisContainer =
        GenericContainer<Nothing>("redis:6.0.6").apply {
            withExposedPorts(6379)
            start()
            println("Redis port: ${getMappedPort(6379)}")
        }

    val mysqlContainer =
        GenericContainer<Nothing>("mysql:8").apply {
            withExposedPorts(3306)

            // Find init sql handling common run directories
            val apiRelPath = "docker/mysql-init/1init.sql"
            val initSqlFile =
                listOf("./", "../api/", "api/")
                    .map { File(it + apiRelPath) }
                    .find { it.exists() }
                    ?: throw IllegalStateException(
                        "Init SQL file not found at: $apiRelPath",
                    )

            withCopyFileToContainer(
                MountableFile.forHostPath(initSqlFile.toPath()),
                "/docker-entrypoint-initdb.d/1init.sql",
            )
            withEnv("MYSQL_ROOT_PASSWORD", "password")
            start()
        }
}
