package edu.wgu.osmt.security

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jwt.JwtDecoder

/**
 * JwtDecoder that accepts admin tokens and OAuth2 session tokens.
 *
 * When both single-auth and OAuth2 are enabled (staging), tries admin token
 * decoder first (issuer osmt-admin), then session token decoder (issuer baseUrl).
 *
 * @see AdminAuthController for the login endpoint that issues admin tokens
 * @see SessionTokenService for OAuth2 session token creation
 */
@Configuration
@Profile("oauth2 & single-auth")
class SingleAuthAwareJwtDecoderConfig(
    @Qualifier("adminTokenJwtDecoder") private val adminTokenJwtDecoder: JwtDecoder,
    @Qualifier("sessionTokenJwtDecoder") private val sessionTokenJwtDecoder: JwtDecoder,
) {
    @Bean
    fun jwtDecoder(): JwtDecoder =
        JwtDecoder { token ->
            try {
                adminTokenJwtDecoder.decode(token)
            } catch (_: Exception) {
                sessionTokenJwtDecoder.decode(token)
            }
        }
}
