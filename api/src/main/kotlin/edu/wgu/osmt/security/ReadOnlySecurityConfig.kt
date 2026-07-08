package edu.wgu.osmt.security

import edu.wgu.osmt.config.AppConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * Security for the read-only public instance: no authentication; mutating and
 * authenticated-only routes return 403 via [ReadOnlyAccessDeniedHandler].
 */
@Configuration
@EnableWebSecurity
@Profile("readonly & !oauth2")
class ReadOnlySecurityConfig(
    private val appConfig: AppConfig,
    private val readOnlyAccessDeniedHandler: ReadOnlyAccessDeniedHandler,
) {
    @Bean
    fun readOnlySecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors()
            .and()
            .csrf()
            .disable()
            .httpBasic()
            .disable()
            .formLogin()
            .disable()
            .logout()
            .disable()
        SecurityConfigHelper.configureReadOnlyEndpoints(http, appConfig)
        http.exceptionHandling {
            it.accessDeniedHandler(readOnlyAccessDeniedHandler)
        }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        val origins =
            appConfig.corsAllowedOrigins
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        configuration.allowedOrigins = origins
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
        val source: UrlBasedCorsConfigurationSource = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
