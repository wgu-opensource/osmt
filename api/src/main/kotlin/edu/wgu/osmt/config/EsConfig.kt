package edu.wgu.osmt.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "es", ignoreInvalidFields = true)
data class EsConfig(val uri: String, val scheme: String)
