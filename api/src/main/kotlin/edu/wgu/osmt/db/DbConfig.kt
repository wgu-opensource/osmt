package edu.wgu.osmt.db

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "db", ignoreInvalidFields = true)
data class DbConfig(
    val name: String,
    val uri: String,
    val composedUrl: String,
    val showMissingCreateTableStatements: Boolean = false
)
