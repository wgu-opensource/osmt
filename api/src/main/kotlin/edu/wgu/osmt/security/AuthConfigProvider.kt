package edu.wgu.osmt.security

import edu.wgu.osmt.config.AppConfig
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.stereotype.Component

/**
 * Provides OAuth provider information for the whitelabel API.
 * Uses ClientRegistrationRepository when available (oauth2 profile).
 * Iterates all registrations; custom providers appear without code changes.
 *
 * The generic `oidc` registration carries a configurable display name and
 * optional icon (via app.oauth2.oidc.*); okta/google keep their built-in
 * labels and no server-supplied icon.
 */
@Component
class AuthConfigProvider {
    @Autowired
    lateinit var clientRegistrationRepositoryProvider: ObjectProvider<ClientRegistrationRepository>

    @Autowired
    lateinit var appConfig: AppConfig

    @Value("\${app.baseUrl:http://localhost:8080}")
    lateinit var baseUrl: String

    fun getOAuthProviders(): List<AuthProviderInfo> {
        val repo = clientRegistrationRepositoryProvider.getIfAvailable() ?: return emptyList()

        val providers = mutableListOf<AuthProviderInfo>()
        val iterable = repo as? Iterable<ClientRegistration> ?: return providers
        for (registration in iterable) {
            if (registration.clientId != "xxxxxx") {
                val id = registration.registrationId
                providers.add(
                    AuthProviderInfo(
                        id = id,
                        name = getDisplayName(id),
                        authorizationUrl = "$baseUrl/oauth2/authorization/$id",
                        iconUrl = iconUrlFor(id),
                        iconSlug = iconSlugFor(id),
                    ),
                )
            }
        }
        return providers
    }

    private fun getDisplayName(registrationId: String): String =
        when (registrationId) {
            GENERIC_OIDC_ID -> {
                appConfig.oidcProviderName
            }

            else -> {
                KNOWN_PROVIDERS[registrationId] ?: registrationId.replaceFirstChar {
                    it.uppercase()
                }
            }
        }

    // Icon is configurable only for the generic OIDC slot. okta/google get their
    // built-in marks from the frontend, so the server supplies no icon for them.
    private fun iconUrlFor(registrationId: String): String? =
        appConfig.oidcIconUrl.takeIf { registrationId == GENERIC_OIDC_ID && it.isNotBlank() }

    private fun iconSlugFor(registrationId: String): String? =
        appConfig.oidcIconSlug.takeIf { registrationId == GENERIC_OIDC_ID && it.isNotBlank() }

    companion object {
        private const val GENERIC_OIDC_ID = "oidc"
        private val KNOWN_PROVIDERS =
            mapOf("google" to "Google", "okta" to "Okta")
    }
}

data class AuthProviderInfo(
    val id: String,
    val name: String,
    val authorizationUrl: String,
    val iconUrl: String? = null,
    val iconSlug: String? = null,
)
