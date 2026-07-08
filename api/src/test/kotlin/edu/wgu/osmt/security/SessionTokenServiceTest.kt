package edu.wgu.osmt.security

import edu.wgu.osmt.SpringTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.test.context.ActiveProfiles
import java.time.Instant

/**
 * Verifies SessionTokenService creates valid signed JWTs from OidcUser.
 * Regression for "Failed to select a JWK signing key" when using NimbusJwtEncoder.
 */
@ActiveProfiles("test", "apiserver", "oauth2")
internal class SessionTokenServiceTest : SpringTest() {
    @Autowired
    lateinit var sessionTokenService: SessionTokenService

    @Test
    fun `createToken produces valid signed JWT from OidcUser`() {
        val oidcUser =
            createMockOidcUser(
                subject = "google-123",
                email = "user@example.com",
                name = "Test User",
                roles = listOf("ROLE_Osmt_View"),
            )
        val token = sessionTokenService.createToken(oidcUser)
        assertThat(token).isNotBlank()
        assertThat(token).contains(".")
        val parts = token.split(".")
        assertThat(parts).hasSize(3)
        assertThat(parts[0]).isNotBlank()
        assertThat(parts[1]).isNotBlank()
        assertThat(parts[2]).isNotBlank()
    }

    private fun createMockOidcUser(
        subject: String,
        email: String,
        name: String,
        roles: List<String>,
    ): OidcUser {
        val idToken =
            OidcIdToken
                .withTokenValue("mock-id-token")
                .subject(subject)
                .issuer("https://accounts.google.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build()
        val userInfo =
            OidcUserInfo
                .builder()
                .subject(subject)
                .email(email)
                .name(name)
                .build()
        val authorities: MutableList<GrantedAuthority> =
            roles.map { OAuth2UserAuthority(it, mapOf("sub" to subject)) }.toMutableList()
        return DefaultOidcUser(authorities, idToken, userInfo)
    }
}
