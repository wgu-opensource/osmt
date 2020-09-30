package edu.wgu.osmt

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.MountableFile

/**
 * Base class for unit tests that provides dockerized servers
 */
abstract class BaseDockerizedTest {

    object LongLivedContainers {
        val elasticContainer = GenericContainer<Nothing>("docker.elastic.co/elasticsearch/elasticsearch:7.8.1").apply{
            withExposedPorts(9200, 9300)
            withEnv("discovery.type","single-node")
            withEnv("net","host")
            start()
        }

        val redisContainer = GenericContainer<Nothing>("redis:6.0.6").apply{
            withExposedPorts(6379)
            start()
        }

        val mysqlContainer = GenericContainer<Nothing>("mysql:8").apply {
            withExposedPorts(3306)
            withCopyFileToContainer(
                MountableFile.forHostPath("../docker/mysql-init/1init.sql"),
                "/docker-entrypoint-initdb.d/1init.sql"
            )
            withEnv("MYSQL_ROOT_PASSWORD", "password")
            start()
        }
    }

    companion object {
        @DynamicPropertySource
        @JvmStatic
        fun dynamicProperties(registry: DynamicPropertyRegistry){
            registry.add("db.uri", {"root:password@${LongLivedContainers.mysqlContainer.host}:${LongLivedContainers.mysqlContainer.getMappedPort(3306)}"})
            registry.add("redis.uri", {"${LongLivedContainers.redisContainer.host}:${LongLivedContainers.redisContainer.getMappedPort(6379)}"})
            registry.add("es.uri", {"${LongLivedContainers.elasticContainer.host}:${LongLivedContainers.elasticContainer.getMappedPort(9200)}"})
        }
    }
}


