package edu.wgu.osmt.config

import edu.wgu.osmt.db.DbConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app", ignoreInvalidFields = true)
class AppConfig(
    @Value("\${app.whiteLabelEnabled}")
    val whiteLabelEnabled: Boolean,

    @Value("\${app.baseDomain}")
    val baseDomain: String,

    @Value("\${app.baseUrl}")
    val baseUrl: String,

    @Value("\${app.defaultAuthorName}")
    val defaultAuthorName: String,

    @Value("\${app.defaultAuthorUri}")
    val defaultAuthorUri: String,

    @Value("\${app.defaultCreatorUri}")
    val defaultCreatorUri: String,

    @Value("\${app.frontendUrl}")
    val frontendUrl: String,

    @Value("\${app.loginSuccessRedirectUrl}")
    val loginSuccessRedirectUrl: String
) {
    @Autowired
    lateinit var environment: Environment

    @Autowired
    lateinit var dbConfig: DbConfig

}
