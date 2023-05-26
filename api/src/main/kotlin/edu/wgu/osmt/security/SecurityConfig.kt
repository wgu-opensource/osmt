package edu.wgu.osmt.security

import com.fasterxml.jackson.databind.ObjectMapper
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.model.ApiError
import edu.wgu.osmt.config.AppConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod.*
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Security configurations
 * - to enable another OAuth provider, include the profile name in place of `OTHER-OAUTH-PROFILE`
 *   in the @Profile annotation
 */
@Configuration
@EnableWebSecurity
@Profile("oauth2-okta | OTHER-OAUTH-PROFILE")
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var appConfig: AppConfig

    @Autowired
    lateinit var redirectToFrontend: RedirectToFrontend

    @Autowired
    lateinit var returnUnauthorized: ReturnUnauthorized

    @Override
    override fun configure(http: HttpSecurity) {
        http
            .cors().and()
            .csrf().disable()
            .httpBasic().disable()
            .authorizeRequests()

            .mvcMatchers(GET, RoutePaths.Latest.SKILL_AUDIT_LOG, RoutePaths.Unversioned.SKILL_AUDIT_LOG, RoutePaths.OldStillSupported.SKILL_AUDIT_LOG).authenticated()
            .mvcMatchers(GET, RoutePaths.Latest.COLLECTION_AUDIT_LOG, RoutePaths.Unversioned.COLLECTION_AUDIT_LOG, RoutePaths.OldStillSupported.SKILL_AUDIT_LOG).authenticated()
            .mvcMatchers(GET, RoutePaths.Latest.TASK_DETAIL_SKILLS, RoutePaths.Unversioned.TASK_DETAIL_SKILLS, RoutePaths.OldStillSupported.SKILL_AUDIT_LOG).authenticated()
            .mvcMatchers(GET, RoutePaths.Latest.TASK_DETAIL_BATCH, RoutePaths.Unversioned.TASK_DETAIL_BATCH, RoutePaths.OldStillSupported.SKILL_AUDIT_LOG).authenticated()
            .mvcMatchers(GET, RoutePaths.Latest.SEARCH_JOBCODES_PATH, RoutePaths.Unversioned.SEARCH_JOBCODES_PATH, RoutePaths.OldStillSupported.SKILL_AUDIT_LOG).authenticated()
            .mvcMatchers(GET, RoutePaths.Latest.SEARCH_KEYWORDS_PATH, RoutePaths.Unversioned.SEARCH_KEYWORDS_PATH, RoutePaths.OldStillSupported.SKILL_AUDIT_LOG).authenticated()

            // public search endpoints
            .mvcMatchers(POST, RoutePaths.Latest.SEARCH_SKILLS, RoutePaths.Unversioned.SEARCH_SKILLS, RoutePaths.OldStillSupported.SKILL_AUDIT_LOG).permitAll()
            .mvcMatchers(POST, RoutePaths.Latest.SEARCH_COLLECTIONS, RoutePaths.Unversioned.SEARCH_COLLECTIONS, RoutePaths.OldStillSupported.SKILL_AUDIT_LOG).permitAll()

            // public canonical URL endpoints
            .mvcMatchers(GET, RoutePaths.Latest.SKILL_DETAIL, RoutePaths.OldStillSupported.SKILL_DETAIL, RoutePaths.Unversioned.SKILL_DETAIL, RoutePaths.OldStillSupported.SKILL_AUDIT_LOG).permitAll()
            .mvcMatchers(GET, RoutePaths.Latest.COLLECTION_DETAIL, RoutePaths.Unversioned.COLLECTION_DETAIL, RoutePaths.OldStillSupported.SKILL_AUDIT_LOG).permitAll()

            .mvcMatchers(POST, RoutePaths.Latest.COLLECTION_SKILLS, RoutePaths.Unversioned.COLLECTION_SKILLS, RoutePaths.OldStillSupported.SKILL_AUDIT_LOG).permitAll()
            .mvcMatchers(GET, RoutePaths.Latest.COLLECTION_CSV, RoutePaths.Unversioned.COLLECTION_CSV, RoutePaths.OldStillSupported.SKILL_AUDIT_LOG).permitAll()
            .mvcMatchers(GET, RoutePaths.Latest.TASK_DETAIL_TEXT, RoutePaths.Unversioned.TASK_DETAIL_TEXT, RoutePaths.OldStillSupported.SKILL_AUDIT_LOG).permitAll()   // public csv results
            .mvcMatchers(GET, RoutePaths.Latest.COLLECTION_XLSX, RoutePaths.Unversioned.COLLECTION_XLSX).permitAll()
            .mvcMatchers(GET, RoutePaths.Latest.TASK_DETAIL_MEDIA, RoutePaths.Unversioned.TASK_DETAIL_MEDIA).permitAll()   // public excel results

            .and().exceptionHandling().authenticationEntryPoint(returnUnauthorized)
            .and().oauth2Login().successHandler(redirectToFrontend)
            .and().oauth2ResourceServer().jwt()

        if (appConfig.enableRoles) {
            configureForRoles(http)
        } else {
            configureForNoRoles(http)
        }
    }

    fun configureForRoles(http: HttpSecurity) {
        val ADMIN = appConfig.roleAdmin
        val CURATOR = appConfig.roleCurator
        val VIEW = appConfig.roleView
        val READ = appConfig.scopeRead

        if (appConfig.allowPublicLists) {
            http.authorizeRequests()
                .mvcMatchers(GET, RoutePaths.Latest.SKILLS_LIST, RoutePaths.Unversioned.SKILLS_LIST, RoutePaths.OldStillSupported.SKILLS_LIST).permitAll()
                .mvcMatchers(GET, RoutePaths.Latest.COLLECTIONS_LIST, RoutePaths.Unversioned.COLLECTIONS_LIST).permitAll()
        } else {
            http.authorizeRequests()
                .mvcMatchers(GET, RoutePaths.Latest.SKILLS_LIST, RoutePaths.Unversioned.SKILLS_LIST, RoutePaths.Unversioned.SKILLS_LIST).hasAnyAuthority(ADMIN, CURATOR, VIEW, READ)
                .mvcMatchers(GET, RoutePaths.Latest.COLLECTIONS_LIST, RoutePaths.Unversioned.COLLECTIONS_LIST, RoutePaths.OldStillSupported.COLLECTIONS_LIST).hasAnyAuthority(ADMIN, CURATOR, VIEW, READ)
        }

        http.authorizeRequests()
            .mvcMatchers(POST, RoutePaths.Latest.SKILL_UPDATE, RoutePaths.Unversioned.SKILL_UPDATE, RoutePaths.OldStillSupported.SKILL_UPDATE).hasAnyAuthority(ADMIN, CURATOR)
            .mvcMatchers(POST, RoutePaths.Latest.SKILLS_CREATE, RoutePaths.Unversioned.SKILLS_CREATE, RoutePaths.OldStillSupported.SKILLS_CREATE).hasAnyAuthority(ADMIN, CURATOR)
            .mvcMatchers(POST, RoutePaths.Latest.SKILL_PUBLISH, RoutePaths.Unversioned.SKILL_PUBLISH, RoutePaths.OldStillSupported.SKILL_PUBLISH).hasAnyAuthority(ADMIN)

            .mvcMatchers(POST, RoutePaths.Latest.COLLECTION_CREATE, RoutePaths.Unversioned.COLLECTION_CREATE, RoutePaths.OldStillSupported.COLLECTION_CREATE).hasAnyAuthority(ADMIN, CURATOR)
            .mvcMatchers(POST, RoutePaths.Latest.COLLECTION_PUBLISH, RoutePaths.Unversioned.COLLECTION_PUBLISH, RoutePaths.OldStillSupported.COLLECTION_PUBLISH).hasAnyAuthority(ADMIN)
            .mvcMatchers(POST, RoutePaths.Latest.COLLECTION_UPDATE, RoutePaths.Unversioned.COLLECTION_UPDATE, RoutePaths.OldStillSupported.COLLECTION_UPDATE).hasAnyAuthority(ADMIN, CURATOR)
            .mvcMatchers(POST, RoutePaths.Latest.COLLECTION_SKILLS_UPDATE, RoutePaths.Unversioned.COLLECTION_SKILLS_UPDATE, RoutePaths.OldStillSupported.COLLECTION_SKILLS_UPDATE).hasAnyAuthority(ADMIN, CURATOR)
            .mvcMatchers(DELETE, RoutePaths.Latest.COLLECTION_REMOVE, RoutePaths.Unversioned.COLLECTION_REMOVE, RoutePaths.OldStillSupported.COLLECTION_REMOVE).hasAnyAuthority(ADMIN)
            .mvcMatchers(GET, RoutePaths.Latest.WORKSPACE_PATH, RoutePaths.Unversioned.WORKSPACE_PATH, RoutePaths.OldStillSupported.WORKSPACE_PATH).hasAnyAuthority(ADMIN, CURATOR)

            .mvcMatchers("/api/**").hasAnyAuthority(ADMIN, CURATOR, VIEW, READ)
    }

    fun configureForNoRoles(http: HttpSecurity) {
        http.authorizeRequests()
            .mvcMatchers(GET, RoutePaths.Latest.SKILLS_LIST, RoutePaths.Unversioned.SKILLS_LIST, RoutePaths.OldStillSupported.SKILLS_LIST).permitAll()
            .mvcMatchers(GET, RoutePaths.Latest.COLLECTIONS_LIST, RoutePaths.Unversioned.COLLECTIONS_LIST, RoutePaths.OldStillSupported.COLLECTIONS_LIST).permitAll()

            .mvcMatchers(POST, RoutePaths.Latest.SKILL_UPDATE, RoutePaths.Unversioned.SKILL_UPDATE, RoutePaths.OldStillSupported.SKILL_UPDATE).authenticated()
            .mvcMatchers(POST, RoutePaths.Latest.SKILLS_CREATE, RoutePaths.Unversioned.SKILLS_CREATE, RoutePaths.OldStillSupported.SKILLS_CREATE).authenticated()
            .mvcMatchers(POST, RoutePaths.Latest.SKILL_PUBLISH, RoutePaths.Unversioned.SKILL_PUBLISH, RoutePaths.OldStillSupported.SKILL_PUBLISH).authenticated()

            .mvcMatchers(POST, RoutePaths.Latest.COLLECTION_CREATE, RoutePaths.Unversioned.COLLECTION_CREATE, RoutePaths.OldStillSupported.COLLECTION_CREATE).authenticated()
            .mvcMatchers(POST, RoutePaths.Latest.COLLECTION_PUBLISH, RoutePaths.Unversioned.COLLECTION_PUBLISH, RoutePaths.OldStillSupported.COLLECTION_CREATE).authenticated()
            .mvcMatchers(POST, RoutePaths.Latest.COLLECTION_UPDATE, RoutePaths.Unversioned.COLLECTION_UPDATE, RoutePaths.OldStillSupported.COLLECTION_CREATE).authenticated()
            .mvcMatchers(POST, RoutePaths.Latest.COLLECTION_SKILLS_UPDATE, RoutePaths.Unversioned.SKILL_UPDATE, RoutePaths.OldStillSupported.COLLECTION_CREATE).authenticated()
            .mvcMatchers(DELETE, RoutePaths.Latest.COLLECTION_REMOVE, RoutePaths.Unversioned.COLLECTION_REMOVE, RoutePaths.OldStillSupported.COLLECTION_CREATE).denyAll()

            // fall-through
            .mvcMatchers("/api/**").permitAll()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource?{
        val  configuration: CorsConfiguration = CorsConfiguration()
        configuration.allowedOrigins = appConfig.corsAllowedOrigins.split(",")
        configuration.allowedMethods = listOf("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH")
        // setAllowCredentials(true) is important, otherwise:
        // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
        configuration.allowCredentials = true
        // setAllowedHeaders is important! Without it, OPTIONS preflight request
        // will fail with 403 Invalid CORS request
        configuration.allowedHeaders = listOf("Authorization", "Cache-Control", "Content-Type")
        configuration.exposedHeaders = listOf("X-Total-Count")
        val  source: UrlBasedCorsConfigurationSource = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}


@Component
class RedirectToFrontend : AuthenticationSuccessHandler {
    @Autowired
    lateinit var appConfig: AppConfig

    override fun onAuthenticationSuccess(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
        val redirectStrategy: DefaultRedirectStrategy = DefaultRedirectStrategy()
        when (authentication?.principal) {
            is OidcUser -> {
                val oidcUser:OidcUser = authentication.principal as OidcUser
                val tokenValue = oidcUser.idToken.tokenValue
                val url = "${appConfig.loginSuccessRedirectUrl}?token=${tokenValue}"
                redirectStrategy.sendRedirect(request, response, url)
            }
        }
    }
}

@Component
class ReturnUnauthorized : AuthenticationEntryPoint {
    override fun commence(request: HttpServletRequest?, response: HttpServletResponse?, authentication: AuthenticationException?) {
        response?.let {
            it.contentType = "application/json"
            it.characterEncoding = "UTF-8"
            val apiError = ApiError("Unauthorized")
            val mapper = ObjectMapper()
            mapper.writeValue(it.writer, apiError)
            it.flushBuffer()
        }
    }

}
