package edu.wgu.osmt.security

import edu.wgu.osmt.config.AppConfig
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.ObjectProvider
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType

/**
 * Unit test for [AuthConfigProvider] display-name and icon resolution.
 * Uses an in-memory ClientRegistrationRepository (which is Iterable) so no
 * Spring context or Docker is required.
 */
internal class AuthConfigProviderTest {
    private lateinit var appConfig: AppConfig

    @BeforeEach
    fun setUp() {
        appConfig = mockk(relaxed = true)
        every { appConfig.oidcProviderName } returns "University SSO"
        every { appConfig.oidcIconUrl } returns ""
        every { appConfig.oidcIconSlug } returns ""
    }

    private fun registration(
        id: String,
        clientId: String,
    ): ClientRegistration =
        ClientRegistration
            .withRegistrationId(id)
            .clientId(clientId)
            .clientSecret("secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/$id")
            .authorizationUri("https://idp.example.com/authorize")
            .tokenUri("https://idp.example.com/token")
            .build()

    private fun providerWith(vararg registrations: ClientRegistration): AuthConfigProvider {
        val repo: ClientRegistrationRepository =
            InMemoryClientRegistrationRepository(registrations.toList())
        val objectProvider = mockk<ObjectProvider<ClientRegistrationRepository>>()
        every { objectProvider.getIfAvailable() } returns repo

        return AuthConfigProvider().apply {
            clientRegistrationRepositoryProvider = objectProvider
            this.appConfig = this@AuthConfigProviderTest.appConfig
            baseUrl = "https://osmt.example.edu"
        }
    }

    @Test
    fun `hides registrations with the xxxxxx sentinel client id`() {
        val provider = providerWith(registration("oidc", "xxxxxx"))
        assertThat(provider.getOAuthProviders()).isEmpty()
    }

    @Test
    fun `okta keeps its built-in display name and no server icon`() {
        val provider = providerWith(registration("okta", "real-client"))
        val result = provider.getOAuthProviders().single()
        assertThat(result.name).isEqualTo("Okta")
        assertThat(result.iconUrl).isNull()
        assertThat(result.iconSlug).isNull()
    }

    @Test
    fun `oidc uses the configured provider name`() {
        val provider = providerWith(registration("oidc", "real-client"))
        val result = provider.getOAuthProviders().single()
        assertThat(result.name).isEqualTo("University SSO")
        assertThat(result.authorizationUrl)
            .isEqualTo("https://osmt.example.edu/oauth2/authorization/oidc")
    }

    @Test
    fun `oidc carries a configured icon url`() {
        every { appConfig.oidcIconUrl } returns "https://cdn.example.edu/sso.svg"
        val provider = providerWith(registration("oidc", "real-client"))
        val result = provider.getOAuthProviders().single()
        assertThat(result.iconUrl).isEqualTo("https://cdn.example.edu/sso.svg")
        assertThat(result.iconSlug).isNull()
    }

    @Test
    fun `oidc carries a configured icon slug`() {
        every { appConfig.oidcIconSlug } returns "openid"
        val provider = providerWith(registration("oidc", "real-client"))
        val result = provider.getOAuthProviders().single()
        assertThat(result.iconSlug).isEqualTo("openid")
        assertThat(result.iconUrl).isNull()
    }

    @Test
    fun `icon config does not leak onto okta`() {
        every { appConfig.oidcIconUrl } returns "https://cdn.example.edu/sso.svg"
        val provider = providerWith(registration("okta", "real-client"))
        val result = provider.getOAuthProviders().single()
        assertThat(result.iconUrl).isNull()
    }
}
