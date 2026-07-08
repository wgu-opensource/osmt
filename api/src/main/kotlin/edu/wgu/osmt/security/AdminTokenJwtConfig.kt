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
 * JWT encoder and decoder for single-auth admin tokens.
 *
 * Admin tokens are signed with HS256 using APP_SESSION_TOKEN_SECRET.
 * Issuer is "osmt-admin" to distinguish from OAuth2 session tokens.
 *
 * @see AdminTokenService for token creation
 * @see AdminUserAuthenticationFilter for Bearer token validation
 */
@Configuration
@Profile("single-auth & !readonly")
class AdminTokenJwtConfig(
    private val appConfig: AppConfig,
) {
    @Bean
    fun adminTokenJwtEncoder(): JwtEncoder {
        val secretBytes = resolveSecretBytes()
        val jwk =
            com.nimbusds.jose.jwk.OctetSequenceKey
                .Builder(secretBytes)
                .keyID("admin-token-key")
                .algorithm(JWSAlgorithm.HS256)
                .build()
        val jwkSet =
            com.nimbusds.jose.jwk
                .JWKSet(jwk)
        val jwkSource = ImmutableJWKSet<SecurityContext>(jwkSet)
        return NimbusJwtEncoder(jwkSource)
    }

    @Bean
    fun adminTokenJwtDecoder(): JwtDecoder {
        val key = resolveSecretKey()
        val decoder =
            NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build()
        decoder.setJwtValidator(
            JwtValidators.createDefaultWithIssuer(AdminTokenService.ADMIN_ISSUER),
        )
        return decoder
    }

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
