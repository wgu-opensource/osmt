package edu.wgu.osmt.security

import edu.wgu.osmt.config.AppConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Creates backend-issued session tokens from OAuth2 OidcUser.
 *
 * Extracts roles from authorities and builds a signed JWT with consistent
 * claims (roles, sub, email) for the frontend.
 */
@Service
@Profile("oauth2")
class SessionTokenService(
    private val appConfig: AppConfig,
    @Qualifier("sessionTokenJwtEncoder") private val jwtEncoder: JwtEncoder,
) {
    fun createToken(oidcUser: OidcUser): String {
        val now = Instant.now()
        val expiresAt =
            now.plusSeconds(appConfig.sessionTokenExpirySeconds)
        val roles =
            oidcUser.authorities
                .map { it.authority }
                .filter { it.startsWith("ROLE_") }
                .joinToString(",")
        val issuer =
            appConfig.sessionTokenIssuer.ifBlank { appConfig.baseUrl }
        val claims =
            JwtClaimsSet
                .builder()
                .issuer(issuer)
                .subject(oidcUser.subject ?: oidcUser.name ?: "unknown")
                .claim("email", oidcUser.email ?: oidcUser.name)
                .claim("name", oidcUser.fullName ?: oidcUser.name)
                .claim("roles", roles.ifEmpty { appConfig.roleView })
                .issuedAt(now)
                .expiresAt(expiresAt)
                .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        val params = JwtEncoderParameters.from(header, claims)
        return jwtEncoder.encode(params).tokenValue
    }
}
