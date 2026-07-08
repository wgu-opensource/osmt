# Phase 2: Unified Security and Whitelabel API

## Scope of phase

Merge OAuth2 and single-auth into one SecurityConfig. Add AuthConfigProvider. Extend whitelabel endpoint with authProviders and singleAuthEnabled.

## Code Organization Reminders

- One concept per file; AuthConfigProvider as separate component
- Place bean definitions before helper methods
- Reuse SecurityConfigHelper; avoid duplicating endpoint rules

## Implementation Details

### 1. Create AuthConfigProvider

Create `api/src/main/kotlin/edu/wgu/osmt/security/AuthConfigProvider.kt`:

```kotlin
package edu.wgu.osmt.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.stereotype.Component

data class AuthProviderInfo(
    val id: String,
    val name: String,
    val authorizationUrl: String,
)

@Component
class AuthConfigProvider {
    @Autowired
    lateinit var clientRegistrationRepository: ClientRegistrationRepository

    @Value("\${app.baseUrl:http://localhost:8080}")
    lateinit var baseUrl: String

    fun getOAuthProviders(): List<AuthProviderInfo> {
        return clientRegistrationRepository.iterator().asSequence()
            .map { reg -> AuthProviderInfo(
                id = reg.registrationId,
                name = reg.clientName.takeIf { it.isNotBlank() } ?: reg.registrationId,
                authorizationUrl = "$baseUrl/oauth2/authorization/${reg.registrationId}",
            ) }
            .toList()
    }
}
```

Handle case where ClientRegistrationRepository is empty (no OAuth providers). Return empty list.

### 2. Conditional AuthConfigProvider

AuthConfigProvider depends on ClientRegistrationRepository. When `oauth2` profile is inactive, that bean may not exist. Use `@Autowired(required = false)` and `@ConditionalOnBean` or make AuthConfigProvider profile-scoped to `oauth2`. Alternatively, create a no-op AuthConfigProvider for non-oauth2 profiles that returns empty list.

Simpler approach: AuthConfigProvider only active when `ClientRegistrationRepository` exists. Use `@ConditionalOnBean(ClientRegistrationRepository::class)` on AuthConfigProvider. UiController autowires with `required = false` and handles null.

### 3. Update UiController whitelabel endpoint

In `UiController.kt`:

```kotlin
@Autowired(required = false)
var authConfigProvider: AuthConfigProvider? = null

@Value("\${app.singleAuthEnabled:false}")
var singleAuthEnabled: Boolean = false
```

In `whitelabelConfig()`:
- Add `authProviders`: list from `authConfigProvider?.getOAuthProviders() ?: emptyList()`
- Add `singleAuthEnabled`: from `appConfig` or new property
- Keep `loginUrl` for backward compat (can derive primary OAuth URL or leave for single-auth)

### 4. Add app.singleAuthEnabled property

In `application.properties`:
```properties
app.singleAuthEnabled=false
```

In `application-single-auth.properties` (or profile where single-auth is on):
```properties
app.singleAuthEnabled=true
```

### 5. Unify SecurityConfig

Modify `SecurityConfig.kt`:
- Change profile to `oauth2` (or a profile that activates when OAuth is configured)
- Add AdminUserAuthenticationFilter before UsernamePasswordAuthenticationFilter when `app.singleAuthEnabled` is true
- Keep oauth2Login(), oauth2ResourceServer(), SecurityConfigHelper rules
- Add conditional: `if (appConfig.singleAuthEnabled) { http.addFilterBefore(adminUserAuthenticationFilter, ...) }`

AdminUserAuthenticationFilter must be `@ConditionalOnProperty("app.singleAuthEnabled")` or similar so it's not active when single-auth is disabled. Or always add the filter; filter checks credentials and only authenticates when valid.

### 6. Remove SingleAuthSecurityConfig as separate profile

- SingleAuthSecurityConfig currently has `@Profile("single-auth")`. With unified config, we either:
  - (A) Remove SingleAuthSecurityConfig entirely; merge its logic into SecurityConfig
  - (B) Keep SingleAuthSecurityConfig but have it work alongside SecurityConfig when both oauth2 and single-auth are active

Design says "Unified SecurityConfig" – so merge into SecurityConfig. SecurityConfig runs when `oauth2` profile is active. When both oauth2 and single-auth: one SecurityConfig handles both. When only single-auth (no OAuth providers): we need a profile that activates SecurityConfig without OAuth. Complexity: profiles are `oauth2` (OAuth present) and `single-auth` (admin login only). For "both", we need both behaviors in one config.

Solution: Use `oauth2` profile when OAuth providers configured. Use `single-auth` OR a combined profile when single-auth enabled. Simplest: have one profile `oauth2` that can include both OAuth and single-auth. If no OAuth registrations but singleAuthEnabled, we still need security – so maybe `oauth2` is always on when we want auth, and we use `single-auth` only when NO OAuth (legacy local dev). For staging: `oauth2` + `single-auth` both active? Spring allows multiple profiles. So `@Profile("oauth2 | single-auth")` on a unified SecurityConfig. When oauth2: OAuth + optionally single-auth. When single-auth only: no OAuth, just AdminUserAuthenticationFilter.

Actually: one SecurityConfig with `@Profile("oauth2 | single-auth")`. In the config:
- Always add AdminUserAuthenticationFilter when single-auth profile active (or app.singleAuthEnabled)
- Always add oauth2Login when oauth2 profile active AND ClientRegistrationRepository has registrations
- If only single-auth: no oauth2Login (would fail with no registrations)

Spring's oauth2Login() auto-configures from ClientRegistrationRepository. If empty, it might create an empty login page. Check Spring behavior.

Safer: Two beans – one SecurityFilterChain for oauth2 (when registrations exist), one for single-auth. But we want ONE chain that does both. Spring Security allows one HttpSecurity configuration. We can:
- oauth2Login() - works when registrations exist
- addFilterBefore(adminUserAuthenticationFilter) - works always

When only single-auth, we must NOT enable oauth2Login if no registrations. Use `@ConditionalOnBean(ClientRegistrationRepository::class)` for the oauth2Login part? Or check at runtime.

Simpler: Keep two SecurityConfig classes – one for oauth2, one for single-auth. They are mutually exclusive by profile. For "both", we need a third config. That's complex.

Better: Single SecurityConfig with profile `oauth2 | single-auth`. In the config:
```kotlin
if (hasOAuthRegistrations()) {
    http.oauth2Login()...
}
if (singleAuthEnabled) {
    http.addFilterBefore(adminUserAuthenticationFilter, ...)
}
```
We'd need to check ClientRegistrationRepository at config time. Possible.

Or: Always add oauth2Login. When no registrations, Spring might no-op. When single-auth only, we still need the filter. Let's try: unified config with both, profile `oauth2 | single-auth`, and see if oauth2Login with empty repo causes issues.

Document: For single-auth only (no OAuth), use `single-auth` profile. For OAuth only, use `oauth2` profile. For both (staging), use `oauth2,single-auth` – SecurityConfig loads when either profile matches, and we enable both OAuth and the filter. AdminAuthController needs `@Profile("single-auth")` to stay – it's only for single-auth login endpoint. So we need single-auth profile for that. So profiles: `oauth2` (OAuth config present) and `single-auth` (single-auth enabled). Both can be active.

Unified SecurityConfig: `@Profile("oauth2 | single-auth")`. Merge the two configs. AdminAuthController stays `@Profile("single-auth")`. When both profiles active: one SecurityFilterChain with oauth2Login + AdminUserAuthenticationFilter.

### 7. Update AppConfig for singleAuthEnabled

Add to AppConfig.kt:
```kotlin
@Value("\${app.singleAuthEnabled:false}")
val singleAuthEnabled: Boolean = false,
```

## Tests

- Add unit test for AuthConfigProvider when repository has registrations
- AuthConfigProvider returns empty list when repository empty

## Validate

```bash
cd /Users/yona/dev/skybridge/osmt
sdk env 2>/dev/null || true
mvn clean test -pl api
```
