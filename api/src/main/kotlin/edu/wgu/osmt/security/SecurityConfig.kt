package edu.wgu.osmt.security

import com.fasterxml.jackson.databind.ObjectMapper
import edu.wgu.osmt.RoutePaths.COLLECTIONS_LIST
import edu.wgu.osmt.RoutePaths.COLLECTION_AUDIT_LOG
import edu.wgu.osmt.RoutePaths.COLLECTION_CREATE
import edu.wgu.osmt.RoutePaths.COLLECTION_CSV
import edu.wgu.osmt.RoutePaths.COLLECTION_DETAIL
import edu.wgu.osmt.RoutePaths.COLLECTION_PUBLISH
import edu.wgu.osmt.RoutePaths.COLLECTION_REMOVE
import edu.wgu.osmt.RoutePaths.COLLECTION_SKILLS
import edu.wgu.osmt.RoutePaths.COLLECTION_SKILLS_UPDATE
import edu.wgu.osmt.RoutePaths.COLLECTION_UPDATE
import edu.wgu.osmt.RoutePaths.COLLECTION_XLSX
import edu.wgu.osmt.RoutePaths.SEARCH_COLLECTIONS
import edu.wgu.osmt.RoutePaths.SEARCH_JOBCODES_PATH
import edu.wgu.osmt.RoutePaths.SEARCH_KEYWORDS_PATH
import edu.wgu.osmt.RoutePaths.SEARCH_SKILLS
import edu.wgu.osmt.RoutePaths.SKILLS_CREATE
import edu.wgu.osmt.RoutePaths.SKILLS_LIST
import edu.wgu.osmt.RoutePaths.SKILL_AUDIT_LOG
import edu.wgu.osmt.RoutePaths.SKILL_DETAIL
import edu.wgu.osmt.RoutePaths.SKILL_PUBLISH
import edu.wgu.osmt.RoutePaths.SKILL_UPDATE
import edu.wgu.osmt.RoutePaths.TASK_DETAIL_BATCH
import edu.wgu.osmt.RoutePaths.TASK_DETAIL_MEDIA
import edu.wgu.osmt.RoutePaths.TASK_DETAIL_SKILLS
import edu.wgu.osmt.RoutePaths.TASK_DETAIL_TEXT
import edu.wgu.osmt.RoutePaths.WORKSPACE_PATH
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

            .mvcMatchers(GET, SKILL_AUDIT_LOG).authenticated()
            .mvcMatchers(GET, COLLECTION_AUDIT_LOG).authenticated()
            .mvcMatchers(GET, TASK_DETAIL_SKILLS).authenticated()
            .mvcMatchers(GET, TASK_DETAIL_BATCH).authenticated()
            .mvcMatchers(GET, SEARCH_JOBCODES_PATH).authenticated()
            .mvcMatchers(GET, SEARCH_KEYWORDS_PATH).authenticated()

            // public search endpoints
            .mvcMatchers(POST, SEARCH_SKILLS).permitAll()
                // .mvcMatchers(POST, SEARCH_COLLECTIONS).permitAll()

            // public canonical URL endpoints
            .mvcMatchers(GET, SKILL_DETAIL).permitAll()
            .mvcMatchers(GET, COLLECTION_DETAIL).permitAll()

            .mvcMatchers(POST, COLLECTION_SKILLS).permitAll()
            .mvcMatchers(GET, COLLECTION_CSV).permitAll()
            .mvcMatchers(GET, TASK_DETAIL_TEXT).permitAll()   // public csv results
            .mvcMatchers(GET, COLLECTION_XLSX).permitAll()
            .mvcMatchers(GET, TASK_DETAIL_MEDIA).permitAll()   // public excel results

            .and().exceptionHandling().authenticationEntryPoint(returnUnauthorized)
            .and().oauth2Login().successHandler(redirectToFrontend)
            .and().oauth2ResourceServer().jwt()

        if (appConfig.allowPublicSearching) {
            http.authorizeRequests().mvcMatchers(POST, SEARCH_COLLECTIONS).permitAll()
        }

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
                .mvcMatchers(GET, SKILLS_LIST).permitAll()
                .mvcMatchers(GET, COLLECTIONS_LIST).permitAll()
        } else {
            http.authorizeRequests()
                .mvcMatchers(GET, SKILLS_LIST).hasAnyAuthority(ADMIN, CURATOR, VIEW, READ)
                .mvcMatchers(GET, COLLECTIONS_LIST).hasAnyAuthority(ADMIN, CURATOR, VIEW, READ)
        }

        http.authorizeRequests()
            .mvcMatchers(POST, SKILL_UPDATE).hasAnyAuthority(ADMIN, CURATOR)
            .mvcMatchers(POST, SKILLS_CREATE).hasAnyAuthority(ADMIN, CURATOR)
            .mvcMatchers(POST, SKILL_PUBLISH).hasAnyAuthority(ADMIN)

            .mvcMatchers(POST, COLLECTION_CREATE).hasAnyAuthority(ADMIN, CURATOR)
            .mvcMatchers(POST, COLLECTION_PUBLISH).hasAnyAuthority(ADMIN)
            .mvcMatchers(POST, COLLECTION_UPDATE).hasAnyAuthority(ADMIN, CURATOR)
            .mvcMatchers(POST, COLLECTION_SKILLS_UPDATE).hasAnyAuthority(ADMIN, CURATOR)
            .mvcMatchers(DELETE, COLLECTION_REMOVE).hasAnyAuthority(ADMIN)
            .mvcMatchers(GET, WORKSPACE_PATH).hasAnyAuthority(ADMIN, CURATOR)

            .mvcMatchers("/api/**").hasAnyAuthority(ADMIN, CURATOR, VIEW, READ)
    }

    fun configureForNoRoles(http: HttpSecurity) {
        http.authorizeRequests()
            .mvcMatchers(GET, SKILLS_LIST).permitAll()
            .mvcMatchers(GET, COLLECTIONS_LIST).permitAll()

            .mvcMatchers(POST, SKILL_UPDATE).authenticated()
            .mvcMatchers(POST, SKILLS_CREATE).authenticated()
            .mvcMatchers(POST, SKILL_PUBLISH).authenticated()

            .mvcMatchers(POST, COLLECTION_CREATE).authenticated()
            .mvcMatchers(POST, COLLECTION_PUBLISH).authenticated()
            .mvcMatchers(POST, COLLECTION_UPDATE).authenticated()
            .mvcMatchers(POST, COLLECTION_SKILLS_UPDATE).authenticated()
            .mvcMatchers(DELETE, COLLECTION_REMOVE).denyAll()

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
