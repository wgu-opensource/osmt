package edu.wgu.osmt.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "db", ignoreInvalidFields = true)
data class DbConfig(
    val name: String,
    val uri: String,
    val composedUri: String,
    val createTablesAndColumnsIfMissing: Boolean = false
)
