package edu.wgu.osmt.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "es", ignoreInvalidFields = true)
data class EsConfig(val host: String, val port: Int, val scheme: String)
