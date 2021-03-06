package edu.wgu.osmt

import edu.wgu.osmt.db.DbConfig
import edu.wgu.osmt.elasticsearch.EsConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
@EnableConfigurationProperties(DbConfig::class, EsConfig::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
