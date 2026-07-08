package edu.wgu.osmt.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import edu.wgu.osmt.config.AppConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import java.util.Base64
import javax.crypto.spec.SecretKeySpec

/**
 * JWT encoder and decoder for OAuth2 session tokens.
 *
 * Backend issues its own signed JWT after OAuth2 login instead of passing
 * the IdP token. Uses HS256 with configurable secret.
 */
@Configuration
@Profile("oauth2")
class SessionTokenJwtConfig(
    private val appConfig: AppConfig,
) {
    @Bean
    fun sessionTokenJwtEncoder(): JwtEncoder {
        val secretBytes = resolveSecretBytes()
        val jwk =
            com.nimbusds.jose.jwk.OctetSequenceKey
                .Builder(secretBytes)
                .keyID("session-token-key")
                .algorithm(JWSAlgorithm.HS256)
                .build()
        val jwkSet =
            com.nimbusds.jose.jwk
                .JWKSet(jwk)
        val jwkSource = ImmutableJWKSet<SecurityContext>(jwkSet)
        return NimbusJwtEncoder(jwkSource)
    }

    @Bean
    fun sessionTokenJwtDecoder(): JwtDecoder {
        val key = resolveSecretKey()
        val decoder =
            NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build()
        val issuer = appConfig.sessionTokenIssuer.ifBlank { appConfig.baseUrl }
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer))
        return decoder
    }

    @Bean
    @Profile("oauth2 & !single-auth")
    fun jwtDecoder(): JwtDecoder = sessionTokenJwtDecoder()

    private fun resolveSecretKey(): javax.crypto.SecretKey {
        val secretBytes = resolveSecretBytes()
        return SecretKeySpec(secretBytes, "HmacSHA256")
    }

    private fun resolveSecretBytes(): ByteArray {
        val secret = appConfig.sessionTokenSecret
        if (secret.isBlank()) {
            throw IllegalStateException(
                "APP_SESSION_TOKEN_SECRET is required when oauth2 or single-auth is " +
                    "active. For local dev, use the 'dev' profile. " +
                    "'openssl rand -base64 32' for production.",
            )
        }
        return Base64.getDecoder().decode(secret)
    }
}
