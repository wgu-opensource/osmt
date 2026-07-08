package edu.wgu.osmt.security

import com.fasterxml.jackson.databind.ObjectMapper
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.model.ApiError
import edu.wgu.osmt.config.AppConfig
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * Security configuration for OAuth2 (profile: oauth2).
 * Add providers via application-oauth2.properties; no code changes required.
 */
@Configuration
@EnableWebSecurity
@Profile("oauth2 & !readonly")
class SecurityConfig {
    @Autowired
    lateinit var appConfig: AppConfig

    @Autowired
    lateinit var redirectToFrontend: RedirectToFrontend

    @Autowired
    lateinit var returnUnauthorized: ReturnUnauthorized

    @Autowired(required = false)
    var adminUserAuthenticationFilter: AdminUserAuthenticationFilter? = null

    @Autowired(required = false)
    var jwtDecoder: JwtDecoder? = null

    private fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val grantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
        grantedAuthoritiesConverter.setAuthoritiesClaimName(appConfig.oauth2RolesClaim)
        grantedAuthoritiesConverter.setAuthorityPrefix("")
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter)
        return converter
    }

    @Bean
    fun userAuthoritiesMapper(): GrantedAuthoritiesMapper =
        GrantedAuthoritiesMapper { authorities ->
            authorities
                .flatMap { authority ->
                    when (authority) {
                        is OidcUserAuthority -> {
                            val claim =
                                authority.idToken.claims[appConfig.oauth2RolesClaim]
                            when (claim) {
                                is Collection<*> -> {
                                    claim
                                        .filterIsInstance<String>()
                                        .map { SimpleGrantedAuthority(it) }
                                }

                                is String -> {
                                    listOf(SimpleGrantedAuthority(claim))
                                }

                                else -> {
                                    listOf(authority)
                                }
                            }
                        }

                        is OAuth2UserAuthority -> {
                            val claim =
                                authority.attributes[appConfig.oauth2RolesClaim]
                            when (claim) {
                                is Collection<*> -> {
                                    claim
                                        .filterIsInstance<String>()
                                        .map { SimpleGrantedAuthority(it) }
                                }

                                is String -> {
                                    listOf(SimpleGrantedAuthority(claim))
                                }

                                else -> {
                                    listOf(authority)
                                }
                            }
                        }

                        else -> {
                            listOf(authority)
                        }
                    }
                }.toMutableSet()
        }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        var config =
            http
                .cors()
                .and()
                .csrf()
                .disable()
                .httpBasic()
                .disable()
                // Configure public and authenticated endpoints using shared helper
                // This ensures consistency with single-auth security configuration
                .also {
                    SecurityConfigHelper.configurePublicEndpoints(it)
                    SecurityConfigHelper.configureAuthenticatedEndpoints(it)
                }.exceptionHandling()
                .authenticationEntryPoint(returnUnauthorized)
                .and()
        if (appConfig.singleAuthEnabled && adminUserAuthenticationFilter != null) {
            config =
                config.addFilterBefore(
                    adminUserAuthenticationFilter!!,
                    UsernamePasswordAuthenticationFilter::class.java,
                )
        }
        config =
            config
                .oauth2Login()
                .successHandler(redirectToFrontend)
                .and()
        config =
            config.oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwtDecoder?.let { jwt.decoder(it) }
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }

        if (appConfig.enableRoles) {
            configureForRoles(config)
        } else {
            configureForNoRoles(config)
        }

        return config.build()
    }

    fun configureForRoles(http: HttpSecurity) {
        val ADMIN = appConfig.roleAdmin
        val CURATOR = appConfig.roleCurator
        val VIEW = appConfig.roleView
        val READ = appConfig.scopeRead

        // Use shared helper to ensure consistency with single-auth configuration
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

    fun configureForNoRoles(http: HttpSecurity) {
        // Use shared helper to ensure consistency with single-auth configuration
        SecurityConfigHelper.configureNoRoleEndpoints(http)
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource? {
        val configuration: CorsConfiguration = CorsConfiguration()
        configuration.allowedOrigins = appConfig.corsAllowedOrigins.split(",")
        configuration.allowedMethods = listOf("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH")
        // setAllowCredentials(true) is important, otherwise:
        // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
        configuration.allowCredentials = true
        // setAllowedHeaders is important! Without it, OPTIONS preflight request
        // will fail with 403 Invalid CORS request
        configuration.allowedHeaders = listOf("Authorization", "Cache-Control", "Content-Type")
        configuration.exposedHeaders = listOf("X-Total-Count")
        val source: UrlBasedCorsConfigurationSource = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}

@Component
@Profile("oauth2")
class RedirectToFrontend : AuthenticationSuccessHandler {
    @Autowired
    lateinit var appConfig: AppConfig

    @Autowired
    lateinit var sessionTokenService: SessionTokenService

    override fun onAuthenticationSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication?,
    ) {
        val redirectStrategy: DefaultRedirectStrategy = DefaultRedirectStrategy()
        when (authentication?.principal) {
            is OidcUser -> {
                val oidcUser: OidcUser = authentication.principal as OidcUser
                val token = sessionTokenService.createToken(oidcUser)
                val url = "${appConfig.loginSuccessRedirectUrl}?token=$token"
                redirectStrategy.sendRedirect(request, response, url)
            }
        }
    }
}

@Component
class ReturnUnauthorized : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: AuthenticationException?,
    ) {
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
