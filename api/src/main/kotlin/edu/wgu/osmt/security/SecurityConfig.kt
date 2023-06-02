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

            .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.SKILL_AUDIT_LOG}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.SKILL_AUDIT_LOG}",
                    "${RoutePaths.API}${RoutePaths.SKILL_AUDIT_LOG}").authenticated()
            .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_AUDIT_LOG}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTION_AUDIT_LOG}",
                    "${RoutePaths.API}${RoutePaths.COLLECTION_AUDIT_LOG}").authenticated()
            .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.TASK_DETAIL_SKILLS}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.TASK_DETAIL_SKILLS}",
                    "${RoutePaths.API}${RoutePaths.TASK_DETAIL_SKILLS}").authenticated()
            .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.TASK_DETAIL_BATCH}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.TASK_DETAIL_BATCH}",
                    "${RoutePaths.API}${RoutePaths.TASK_DETAIL_BATCH}").authenticated()
            .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.SEARCH_JOBCODES_PATH}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.SEARCH_JOBCODES_PATH}",
                    "${RoutePaths.API}${RoutePaths.SEARCH_JOBCODES_PATH}").authenticated()
            .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.SEARCH_KEYWORDS_PATH}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.SEARCH_KEYWORDS_PATH}",
                    "${RoutePaths.API}${RoutePaths.SEARCH_KEYWORDS_PATH}").authenticated()

            // public search endpoints
            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.SEARCH_SKILLS}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.SEARCH_SKILLS}",
                    "${RoutePaths.API}${RoutePaths.SEARCH_SKILLS}").permitAll()
            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.SEARCH_COLLECTIONS}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.SEARCH_COLLECTIONS}",
                    "${RoutePaths.API}${RoutePaths.SEARCH_COLLECTIONS}").permitAll()

            // public canonical URL endpoints
            .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.SKILL_DETAIL}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.SKILL_DETAIL}",
                    "${RoutePaths.API}${RoutePaths.SKILL_DETAIL}").permitAll()
            .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_DETAIL}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTION_DETAIL}",
                    "${RoutePaths.API}${RoutePaths.COLLECTION_DETAIL}").permitAll()

            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_SKILLS}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTION_SKILLS}",
                    "${RoutePaths.API}${RoutePaths.COLLECTION_SKILLS}").permitAll()
            .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_CSV}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTION_CSV}",
                    "${RoutePaths.API}${RoutePaths.COLLECTION_CSV}").permitAll()
            .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.TASK_DETAIL_TEXT}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.TASK_DETAIL_TEXT}",
                    "${RoutePaths.API}${RoutePaths.TASK_DETAIL_TEXT}").permitAll()
            .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_XLSX}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTION_XLSX}",
                    "${RoutePaths.API}${RoutePaths.COLLECTION_XLSX}").permitAll()
            .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.TASK_DETAIL_MEDIA}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.TASK_DETAIL_MEDIA}",
                    "${RoutePaths.API}${RoutePaths.TASK_DETAIL_MEDIA}").permitAll()

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
                .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.SKILLS_LIST}",
                        "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.SKILLS_LIST}",
                        "${RoutePaths.API}${RoutePaths.SKILLS_LIST}").permitAll()
                .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTIONS_LIST}",
                        "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTIONS_LIST}",
                        "${RoutePaths.API}${RoutePaths.COLLECTIONS_LIST}").permitAll()
        } else {
            http.authorizeRequests()
                .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.SKILLS_LIST}",
                        "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.SKILLS_LIST}",
                        "${RoutePaths.API}${RoutePaths.SKILLS_LIST}").hasAnyAuthority(ADMIN, CURATOR, VIEW, READ)
                .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTIONS_LIST}",
                        "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTIONS_LIST}",
                        "${RoutePaths.API}${RoutePaths.COLLECTIONS_LIST}").hasAnyAuthority(ADMIN, CURATOR, VIEW, READ)
        }

        http.authorizeRequests()
            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.SKILL_UPDATE}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.SKILL_UPDATE}",
                    "${RoutePaths.API}${RoutePaths.SKILL_UPDATE}").hasAnyAuthority(ADMIN, CURATOR)
            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.SKILLS_CREATE}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.SKILLS_CREATE}",
                    "${RoutePaths.API}${RoutePaths.SKILLS_CREATE}").hasAnyAuthority(ADMIN, CURATOR)
            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.SKILL_PUBLISH}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.SKILL_PUBLISH}",
                    "${RoutePaths.API}${RoutePaths.SKILL_PUBLISH}").hasAnyAuthority(ADMIN)

            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_CREATE}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTION_CREATE}",
                    "${RoutePaths.API}${RoutePaths.COLLECTION_CREATE}").hasAnyAuthority(ADMIN, CURATOR)
            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_PUBLISH}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTION_PUBLISH}",
                    "${RoutePaths.API}${RoutePaths.COLLECTION_PUBLISH}").hasAnyAuthority(ADMIN)
            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_UPDATE}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTION_UPDATE}",
                    "${RoutePaths.API}${RoutePaths.COLLECTION_UPDATE}").hasAnyAuthority(ADMIN, CURATOR)
            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_SKILLS_UPDATE}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTION_SKILLS_UPDATE}",
                    "${RoutePaths.API}${RoutePaths.COLLECTION_SKILLS_UPDATE}").hasAnyAuthority(ADMIN, CURATOR)
            .mvcMatchers(DELETE, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_REMOVE}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTION_REMOVE}",
                    "${RoutePaths.API}${RoutePaths.COLLECTION_REMOVE}").hasAnyAuthority(ADMIN)
            .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.WORKSPACE_PATH}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.WORKSPACE_PATH}",
                    "${RoutePaths.API}${RoutePaths.WORKSPACE_PATH}").hasAnyAuthority(ADMIN, CURATOR)

            .mvcMatchers("/api/**").hasAnyAuthority(ADMIN, CURATOR, VIEW, READ)
    }

    fun configureForNoRoles(http: HttpSecurity) {
        http.authorizeRequests()
            .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.SKILLS_LIST}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.SKILLS_LIST}",
                    "${RoutePaths.API}${RoutePaths.SKILLS_LIST}").permitAll()
            .mvcMatchers(GET, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTIONS_LIST}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTIONS_LIST}",
                    "${RoutePaths.API}${RoutePaths.COLLECTIONS_LIST}").permitAll()

            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.SKILL_UPDATE}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.SKILL_UPDATE}",
                    "${RoutePaths.API}${RoutePaths.SKILL_UPDATE}").authenticated()
            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.SKILLS_CREATE}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.SKILLS_CREATE}",
                    "${RoutePaths.API}${RoutePaths.SKILLS_CREATE}").authenticated()
            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.SKILL_PUBLISH}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.SKILL_PUBLISH}",
                    "${RoutePaths.API}${RoutePaths.SKILL_PUBLISH}").authenticated()

            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_CREATE}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTION_CREATE}",
                    "${RoutePaths.API}${RoutePaths.COLLECTION_CREATE}").authenticated()
            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_PUBLISH}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTION_PUBLISH}",
                    "${RoutePaths.API}${RoutePaths.COLLECTION_PUBLISH}").authenticated()
            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_UPDATE}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTION_UPDATE}",
                    "${RoutePaths.API}${RoutePaths.COLLECTION_UPDATE}").authenticated()
            .mvcMatchers(POST, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_SKILLS_UPDATE}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTION_SKILLS_UPDATE}",
                    "${RoutePaths.API}${RoutePaths.COLLECTION_SKILLS_UPDATE}").authenticated()
            .mvcMatchers(DELETE, "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_REMOVE}",
                    "${RoutePaths.API}${RoutePaths.OLD_SUPPORTED}${RoutePaths.COLLECTION_REMOVE}",
                    "${RoutePaths.API}${RoutePaths.COLLECTION_REMOVE}").denyAll()

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
