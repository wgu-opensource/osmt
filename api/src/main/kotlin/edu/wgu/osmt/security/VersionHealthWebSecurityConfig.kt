package edu.wgu.osmt.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer

/**
 * Ensures /version and /health are always publicly accessible, bypassing the security
 * filter chain. These endpoints are used for deployment verification and health checks.
 */
@Configuration
class VersionHealthWebSecurityConfig {
    @Bean
    fun versionHealthWebSecurityCustomizer(): WebSecurityCustomizer =
        WebSecurityCustomizer { web ->
            web.ignoring().requestMatchers("/version", "/health")
        }
}
