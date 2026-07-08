package edu.wgu.osmt.security

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Creates signed admin JWT tokens for single-auth login.
 *
 * Tokens include issuer "osmt-admin" and admin claims.
 * Used by AdminAuthController after credential validation.
 */
@Service
@Profile("single-auth & !readonly")
class AdminTokenService(
    @Qualifier("adminTokenJwtEncoder") private val jwtEncoder: JwtEncoder,
) {
    /**
     * Creates a signed admin JWT valid for 1 hour.
     *
     * @return Encoded JWT token value (starts with "admin-jwt-" in payload)
     */
    fun createAdminToken(): String {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(3600)
        val jti = "admin-jwt-${System.currentTimeMillis()}"
        val claims =
            JwtClaimsSet
                .builder()
                .issuer(ADMIN_ISSUER)
                .subject(AdminAuthConstants.ADMIN_EMAIL)
                .claim("email", AdminAuthConstants.ADMIN_EMAIL)
                .claim("name", AdminAuthConstants.ADMIN_NAME)
                .claim("roles", AdminAuthConstants.ADMIN_ROLE)
                .claim("jti", jti)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        val params = JwtEncoderParameters.from(header, claims)
        return jwtEncoder.encode(params).tokenValue
    }

    companion object {
        const val ADMIN_ISSUER = "osmt-admin"
    }
}
