package edu.wgu.osmt.elasticsearch

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "es", ignoreInvalidFields = true)
data class EsConfig(val uri: String)
