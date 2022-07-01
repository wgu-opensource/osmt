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
    val loginSuccessRedirectUrl: String,

    @Value("\${app.allowPublicSearching}")
    val allowPublicSearching: Boolean = true,

    @Value("\${app.allowPublicLists}")
    val allowPublicLists: Boolean = true,

    @Value("\${app.enableRoles}")
    val enableRoles: Boolean = false,

    @Value("\${app.baseLineAuditLogIfEmpty}")
    val baseLineAuditLogIfEmpty: Boolean,

    @Value("\${app.rsd-context-url}")
    val rsdContextUrl: String,

    @Value("\${app.security.cors.allowedOrigins}")
    val corsAllowedOrigins: String,

    //This next values are WGU specific.
    @Value("\${osmt.security.role.admin:Osmt_Admin}")
    val roleAdmin: String,

    @Value("\${osmt.security.role.curator:Osmt_Curator}")
    val roleCurator: String,

    @Value("\${osmt.security.role.view:Osmt_View}")
    val roleView: String,

    @Value("\${osmt.security.scope.read:SCOPE_osmt.read}")
    val scopeRead: String
) {
    @Autowired
    lateinit var environment: Environment

    @Autowired
    lateinit var dbConfig: DbConfig

}
