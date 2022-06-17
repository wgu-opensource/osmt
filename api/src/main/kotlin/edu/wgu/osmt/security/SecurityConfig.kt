package edu.wgu.osmt.security

import com.fasterxml.jackson.databind.ObjectMapper
import edu.wgu.osmt.RoutePaths.COLLECTIONS_LIST
import edu.wgu.osmt.RoutePaths.COLLECTION_AUDIT_LOG
import edu.wgu.osmt.RoutePaths.COLLECTION_CREATE
import edu.wgu.osmt.RoutePaths.COLLECTION_CSV
import edu.wgu.osmt.RoutePaths.COLLECTION_DETAIL
import edu.wgu.osmt.RoutePaths.COLLECTION_PUBLISH
import edu.wgu.osmt.RoutePaths.COLLECTION_SKILLS
import edu.wgu.osmt.RoutePaths.COLLECTION_SKILLS_UPDATE
import edu.wgu.osmt.RoutePaths.COLLECTION_UPDATE
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
import edu.wgu.osmt.RoutePaths.TASK_DETAIL_SKILLS
import edu.wgu.osmt.RoutePaths.TASK_DETAIL_TEXT
import edu.wgu.osmt.RoutePaths.scrubForConfigure
import edu.wgu.osmt.api.model.ApiError
import edu.wgu.osmt.config.AppConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.server.resource.authentication.DelegatingJwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
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
@Profile("oauth2-okta | oauth2-custom")
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

            // authorization required
            .antMatchers(HttpMethod.POST, SKILLS_CREATE)
                .hasAnyAuthority(appConfig.roleAdmin)
            .antMatchers(HttpMethod.POST, scrubForConfigure(SKILL_UPDATE))
                .hasAnyAuthority(appConfig.roleAdmin, appConfig.roleCurator)
            .antMatchers(HttpMethod.POST, scrubForConfigure(SKILL_PUBLISH))
                .hasAnyAuthority(appConfig.roleAdmin)
            .antMatchers(HttpMethod.POST, scrubForConfigure(SKILL_AUDIT_LOG)).authenticated()
            .antMatchers(HttpMethod.POST, COLLECTION_CREATE)
                .hasAnyAuthority(appConfig.roleAdmin)
            .antMatchers(HttpMethod.POST, scrubForConfigure(COLLECTION_PUBLISH))
                .hasAnyAuthority(appConfig.roleAdmin)
            .antMatchers(HttpMethod.POST, scrubForConfigure(COLLECTION_UPDATE))
                .hasAnyAuthority(appConfig.roleAdmin, appConfig.roleCurator)
            .antMatchers(HttpMethod.POST, scrubForConfigure(COLLECTION_SKILLS_UPDATE))
                .hasAnyAuthority(appConfig.roleAdmin)
            .antMatchers(HttpMethod.POST, scrubForConfigure(COLLECTION_AUDIT_LOG)).permitAll()
            .antMatchers(HttpMethod.GET, scrubForConfigure(TASK_DETAIL_SKILLS)).permitAll()
            .antMatchers(HttpMethod.GET, scrubForConfigure(TASK_DETAIL_BATCH)).permitAll()

            .antMatchers(HttpMethod.GET, SEARCH_JOBCODES_PATH).authenticated()
            .antMatchers(HttpMethod.GET, SEARCH_KEYWORDS_PATH).authenticated()

            .antMatchers(HttpMethod.GET,  SKILLS_LIST)
                .hasAnyAuthority(appConfig.roleAdmin, appConfig.roleCurator, appConfig.roleView, appConfig.scopeRead)
            .antMatchers(HttpMethod.GET,  COLLECTIONS_LIST)
                .hasAnyAuthority(appConfig.roleAdmin, appConfig.roleCurator, appConfig.roleView, appConfig.scopeRead)

            // public search endpoints
            .antMatchers(HttpMethod.POST, SEARCH_SKILLS).permitAll()
            .antMatchers(HttpMethod.POST, SEARCH_COLLECTIONS).permitAll()

            // public canonical URL endpoints
            .antMatchers(HttpMethod.GET, scrubForConfigure(SKILL_DETAIL)).permitAll()
            .antMatchers(HttpMethod.GET, scrubForConfigure(COLLECTION_DETAIL)).permitAll()
            .antMatchers(HttpMethod.POST, scrubForConfigure(COLLECTION_SKILLS)).permitAll()
            .antMatchers(HttpMethod.GET, scrubForConfigure(COLLECTION_CSV)).permitAll()
            .antMatchers(HttpMethod.GET, scrubForConfigure(TASK_DETAIL_TEXT)).permitAll()   // public csv results

            // catch-all
            .antMatchers("/**").permitAll()

            .and().exceptionHandling().authenticationEntryPoint(returnUnauthorized)
            .and().oauth2Login().successHandler(redirectToFrontend)
            .and().oauth2ResourceServer().jwt()
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
