package edu.wgu.osmt.security

import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.security.SecurityConfigHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * Security configuration for single-auth profile.
 *
 * This configuration allows local development and testing without requiring an OAuth2 provider,
 * while still enforcing role-based authorization using test roles injected via
 * [AdminUserAuthenticationFilter].
 *
 * **Security Warning**: This profile is intended for development and testing only.
 * **DO NOT** use this profile in production environments as it bypasses OAuth2 authentication.
 *
 * The single-auth profile:
 * - Disables OAuth2 login and resource server configuration
 * - Uses [AdminUserAuthenticationFilter] to validate admin credentials from Authorization header,
 *   or environment variables
 * - Enforces the same role-based authorization rules as the OAuth2 profile
 * - Supports both `enableRoles=true` and `enableRoles=false` modes
 *
 * Test roles can be provided via:
 * - HTTP header: `X-Test-Role: ROLE_Osmt_Admin`
 * - Query parameter: `?testRole=ROLE_Osmt_Admin`
 * - Environment variable: `TEST_ROLE=ROLE_Osmt_Admin`
 * - Default: `app.test.defaultRole` property (defaults to `ROLE_Osmt_Admin`)
 *
 * @see AdminUserAuthenticationFilter for admin authentication mechanism
 * @see SecurityConfig for OAuth2-based security configuration
 */
@Configuration
@EnableWebSecurity
@Profile("single-auth & !oauth2 & !readonly")
class SingleAuthSecurityConfig {
    @Autowired
    lateinit var appConfig: AppConfig

    @Autowired
    lateinit var adminUserAuthenticationFilter: AdminUserAuthenticationFilter

    @Autowired
    lateinit var singleAuthEntryPoint: SingleAuthEntryPoint

    /**
     * Configures the security filter chain for the single-auth profile.
     *
     * This method sets up:
     * 1. CORS configuration
     * 2. CSRF and HTTP Basic authentication disabled
     * 3. [AdminUserAuthenticationFilter] added before authorization checks
     * 4. Public endpoints (search, canonical URLs) configured to permitAll()
     * 5. Authenticated endpoints (audit logs, task details) require authentication
     * 6. Role-based or simple authentication-based authorization based on [AppConfig.enableRoles]
     *
     * The filter chain order is critical:
     * - AdminUserAuthenticationFilter runs first to validate admin authentication
     * - Authorization checks run after authentication is established
     *
     * @param http HttpSecurity builder
     * @return Configured SecurityFilterChain
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors()
            .and()
            .csrf()
            .disable()
            .httpBasic()
            .disable()
            // Add test role authentication filter before authorization
            .addFilterBefore(
                adminUserAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java,
            )
            // Configure public and authenticated endpoints using shared helper
            // This ensures consistency with OAuth2 security configuration
            .also {
                SecurityConfigHelper.configurePublicEndpoints(it)
                SecurityConfigHelper.configureAuthenticatedEndpoints(it)
            }.exceptionHandling()
            .authenticationEntryPoint(singleAuthEntryPoint)

        if (appConfig.enableRoles) {
            configureForRoles(http)
        } else {
            configureForNoRoles(http)
        }

        return http.build()
    }

    /**
     * Configures role-based authorization rules.
     *
     * When [AppConfig.enableRoles] is true, this method enforces role-based access control:
     * - List endpoints: Public if [AppConfig.allowPublicLists] is true, otherwise require
     *   ADMIN, CURATOR, VIEW, or READ roles
     * - Skill operations: ADMIN and CURATOR can create/update, only ADMIN can publish
     * - Collection operations: ADMIN and CURATOR can create/update, only ADMIN can publish/delete
     * - Workspace: Requires ADMIN or CURATOR role
     * - All other API endpoints: Require ADMIN, CURATOR, VIEW, or READ roles
     *
     * This configuration matches the authorization rules in [SecurityConfig.configureForRoles]
     * to ensure consistent behavior between OAuth2 and single-auth profiles.
     *
     * @param http HttpSecurity builder to configure
     */
    private fun configureForRoles(http: HttpSecurity) {
        val ADMIN = appConfig.roleAdmin
        val CURATOR = appConfig.roleCurator
        val VIEW = appConfig.roleView
        val READ = appConfig.scopeRead

        // Use shared helper to ensure consistency with OAuth2 configuration
        SecurityConfigHelper.configureListEndpoints(
            http,
            appConfig.allowPublicLists,
            ADMIN,
            CURATOR,
            VIEW,
            READ,
        )
        SecurityConfigHelper.configureRoleBasedEndpoints(
            http,
            ADMIN,
            CURATOR,
            VIEW,
            READ,
        )
    }

    /**
     * Configures simple authentication-based authorization (no role checks).
     *
     * When [AppConfig.enableRoles] is false, this method enforces simple authentication:
     * - List endpoints: Public (permitAll)
     * - Write operations (create, update, publish): Require authentication (any authenticated user)
     * - Collection deletion: Denied (denyAll) - prevents accidental deletion
     * - All other endpoints: Public (permitAll)
     *
     * This configuration matches the authorization rules in [SecurityConfig.configureForNoRoles]
     * to ensure consistent behavior between OAuth2 and single-auth profiles.
     *
     * @param http HttpSecurity builder to configure
     */
    private fun configureForNoRoles(http: HttpSecurity) {
        // Use shared helper to ensure consistency with OAuth2 configuration
        SecurityConfigHelper.configureNoRoleEndpoints(http)
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings.
     *
     * Allows requests from origins specified in [AppConfig.corsAllowedOrigins].
     * Includes test headers (X-Test-Role, X-Test-User-Name, X-Test-User-Email) in allowed headers
     * to support admin authentication in single-auth mode.
     *
     * **Security Note**: The admin auth headers are included to support single-auth mode.
     * In production OAuth2 mode, these headers should not be accepted.
     *
     * @return CorsConfigurationSource with configured CORS settings
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource? {
        val configuration: CorsConfiguration = CorsConfiguration()
        configuration.allowedOrigins = appConfig.corsAllowedOrigins.split(",")
        configuration.allowedMethods =
            listOf("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH")
        configuration.allowCredentials = true
        configuration.allowedHeaders =
            listOf(
                "Authorization",
                "Cache-Control",
                "Content-Type",
            )
        configuration.exposedHeaders = listOf("X-Total-Count")
        val source: UrlBasedCorsConfigurationSource =
            UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
