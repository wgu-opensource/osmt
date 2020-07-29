package edu.wgu.osmt.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "db", ignoreInvalidFields = true)
data class DbConfig(
    val host: String,
    val port: String,
    val username: String,
    val password: String,
    val name: String,
    val url: String,
    val createTablesAndColumnsIfMissing: Boolean = false
)
