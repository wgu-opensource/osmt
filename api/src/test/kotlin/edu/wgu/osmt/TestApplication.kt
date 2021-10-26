package edu.wgu.osmt

import edu.wgu.osmt.db.DbConfig
import edu.wgu.osmt.elasticsearch.EsConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
@EnableConfigurationProperties(DbConfig::class, EsConfig::class)
class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}
