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
    @Value("\${app.loginUrl:}")
    val loginUrl: String,
    @Value("\${app.authMode:oauth2}")
    val authMode: String,
    @Value("\${app.singleAuthEnabled:false}")
    val singleAuthEnabled: Boolean = false,
    @Value("\${app.oauth2.rolesClaim:roles}")
    val oauth2RolesClaim: String,
    @Value("\${app.loginSuccessRedirectUrl}")
    val loginSuccessRedirectUrl: String,
    @Value("\${app.userName:name}")
    val userName: String,
    @Value("\${app.userIdentifier:email}")
    val userIdentifier: String,
    @Value("\${app.allowPublicSearching}")
    val allowPublicSearching: Boolean = true,
    @Value("\${app.allowPublicLists}")
    val allowPublicLists: Boolean = true,
    @Value("\${app.publicKeywordLimit:1000}")
    val publicKeywordLimit: Int = 1000,
    @Value("\${app.enableRoles}")
    val enableRoles: Boolean = false,
    @Value("\${app.baseLineAuditLogIfEmpty}")
    val baseLineAuditLogIfEmpty: Boolean,
    @Value("\${app.rsd-context-url}")
    val rsdContextUrl: String,
    @Value("\${app.security.cors.allowedOrigins}")
    val corsAllowedOrigins: String,
    // Single-auth profile admin credentials
    @Value("\${app.single-auth.admin-username:#{null}}")
    val singleAuthAdminUsername: String?,
    @Value("\${app.single-auth.admin-password:#{null}}")
    val singleAuthAdminPassword: String?,
    // This next values are WGU specific.
    @Value("\${osmt.security.role.admin:Osmt_Admin}")
    val roleAdmin: String,
    @Value("\${osmt.security.role.curator:Osmt_Curator}")
    val roleCurator: String,
    @Value("\${osmt.security.role.view:Osmt_View}")
    val roleView: String,
    @Value("\${osmt.security.scope.read:SCOPE_osmt.read}")
    val scopeRead: String,
    // Session token (OAuth2 backend-issued JWT)
    @Value("\${app.sessionTokenSecret:}")
    val sessionTokenSecret: String,
    @Value("\${app.sessionTokenExpirySeconds:86400}")
    val sessionTokenExpirySeconds: Long,
    @Value("\${app.sessionTokenIssuer:}")
    val sessionTokenIssuer: String,
    @Value("\${app.readOnlyMode:false}")
    val readOnlyMode: Boolean = false,
    @Value("\${app.publicInstanceUrl:}")
    val publicInstanceUrl: String,
    @Value("\${app.authoringWelcomeMessage:}")
    val authoringWelcomeMessage: String,
    // Generic OIDC provider (oidc registration) button branding. Kept at the end
    // with defaults so positional test constructors stay valid; Spring injects
    // these via @Value regardless of the Kotlin defaults.
    @Value("\${app.oauth2.oidc.providerName:Single sign-on}")
    val oidcProviderName: String = "Single sign-on",
    @Value("\${app.oauth2.oidc.iconUrl:}")
    val oidcIconUrl: String = "",
    @Value("\${app.oauth2.oidc.iconSlug:}")
    val oidcIconSlug: String = "",
) {
    @Autowired
    lateinit var environment: Environment

    @Autowired
    lateinit var dbConfig: DbConfig
}
