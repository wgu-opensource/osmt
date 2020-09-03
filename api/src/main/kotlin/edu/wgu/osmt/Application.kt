package edu.wgu.osmt

import edu.wgu.osmt.db.DbConfig
import edu.wgu.osmt.elasticsearch.EsConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = arrayOf(DataSourceAutoConfiguration::class, FlywayAutoConfiguration::class))
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
@EnableConfigurationProperties(DbConfig::class, EsConfig::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
