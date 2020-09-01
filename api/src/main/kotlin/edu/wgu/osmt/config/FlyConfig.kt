package edu.wgu.osmt.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "spring.flyway", ignoreInvalidFields = true)
data class FlyConfig(val enabled: Boolean, val locations: String) {

    @Value("\${spring.flyway.baseline-on-migrate}")
    var baselineOnMigrate: Boolean = false
        protected set

    @Value("\${spring.flyway.default-schema}")
    lateinit var defaultSchema: String
}
