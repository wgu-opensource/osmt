package edu.wgu.osmt.config

import edu.wgu.osmt.db.DbConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app", ignoreInvalidFields = true)
class AppConfig {

    @Value("\${app.baseDomain}")
    lateinit var baseDomain: String

    @Value("\${app.baseUrl}")
    lateinit var baseUrl: String

    @Value("\${app.defaultAuthorName}")
    lateinit var defaultAuthorName: String

    @Value("\${app.defaultAuthorUri}")
    lateinit var defaultAuthorUri: String

    @Value("\${app.defaultCreatorUri}")
    lateinit var defaultCreatorUri: String

    @Value("\${app.frontendUrl}")
    lateinit var frontendUrl: String

    @Value("\${app.loginSuccessRedirectUrl}")
    lateinit var loginSuccessRedirectUrl: String

    @Autowired
    lateinit var environment: Environment

    @Autowired
    lateinit var dbConfig: DbConfig

}
