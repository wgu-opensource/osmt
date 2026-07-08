# Phase 1: Migrate to Standard Spring OAuth2

## Scope of phase

Remove Okta Spring Boot starter and configure OAuth2 via standard Spring Security properties. Add configurable roles claim. Update docker entrypoint for new config format.

## Code Organization Reminders

- Prefer one concept per file
- Place abstract/config first, helpers at bottom
- Keep env var names consistent with existing OAUTH_* pattern

## Implementation Details

### 1. Remove Okta starter from pom.xml

In `api/pom.xml`:
- Remove the `com.okta.spring:okta-spring-boot-starter` dependency
- Remove `okta.version` property if no longer used
- Ensure `spring-security-oauth2-client` remains

### 2. Add application-oauth2.properties

Create `api/src/main/resources/config/application-oauth2.properties`:

```properties
# Generic OAuth2 - providers configured via spring.security.oauth2.client.registration.*
# Example for Google (CommonOAuth2Provider.GOOGLE):
# spring.security.oauth2.client.registration.google.client-id=${OAUTH_GOOGLE_CLIENT_ID}
# spring.security.oauth2.client.registration.google.client-secret=${OAUTH_GOOGLE_CLIENT_SECRET}
#
# Example for Okta (custom provider):
# spring.security.oauth2.client.registration.okta.client-id=${OAUTH_OKTA_CLIENT_ID}
# spring.security.oauth2.client.registration.okta.client-secret=${OAUTH_OKTA_CLIENT_SECRET}
# spring.security.oauth2.client.provider.okta.issuer-uri=${OAUTH_OKTA_ISSUER}
# spring.security.oauth2.client.provider.okta.groups-claim=roles
```

Use placeholders; actual provider configs loaded from profile-specific or env-based files.

### 3. Add application-oauth2-google.properties (example)

Create `api/src/main/resources/config/application-oauth2-google.properties`:

```properties
spring.security.oauth2.client.registration.google.client-id=${OAUTH_GOOGLE_CLIENT_ID:xxxxxx}
spring.security.oauth2.client.registration.google.client-secret=${OAUTH_GOOGLE_CLIENT_SECRET:xxxxxx}
spring.security.oauth2.client.registration.google.scope=openid,profile,email
```

### 4. Migrate application-oauth2-okta to standard format

Rename/rewrite `application-oauth2-okta.properties` to use standard Spring properties:

```properties
spring.security.oauth2.client.registration.okta.client-id=${OAUTH_CLIENTID}
spring.security.oauth2.client.registration.okta.client-secret=${OAUTH_CLIENTSECRET}
spring.security.oauth2.client.provider.okta.issuer-uri=${OAUTH_ISSUER}
spring.security.oauth2.client.provider.okta.groups-claim=roles
```

Preserve OAUTH_CLIENTID, OAUTH_CLIENTSECRET, OAUTH_ISSUER for backward compatibility during migration.

### 5. Add roles claim config to AppConfig

In `AppConfig.kt`, add:

```kotlin
@Value("\${app.oauth2.rolesClaim:roles}")
val oauth2RolesClaim: String,
```

In `application.properties`:

```properties
app.oauth2.rolesClaim=roles
```

### 6. Update OAuthHelper for configurable claim

In `OAuthHelper.kt`, use `appConfig.oauth2RolesClaim` when extracting roles from JWT claims. Spring Security maps `groups-claim` at provider level; OAuthHelper reads from `SecurityContextHolder.getContext().authentication.authorities` which may already include mapped authorities. Verify OAuth2UserService or Jwt mapping uses the claim. If not, add logic to extract from claim when building authorities.

### 7. Update docker_entrypoint.sh

- Replace `okta.oauth2.*` JVM args with `spring.security.oauth2.client.*` equivalents or remove (config via env)
- Support OAUTH_GOOGLE_CLIENT_ID, OAUTH_GOOGLE_CLIENT_SECRET for Google
- Support OAUTH_CLIENTID, OAUTH_CLIENTSECRET, OAUTH_ISSUER for Okta (existing)
- Profile: use `oauth2` when any OAuth provider env vars present

### 8. Update SecurityConfig

Change `@Profile("oauth2-okta | OTHER-OAUTH-PROFILE")` to `@Profile("oauth2")`. Remove Okta-specific wiring; rely on `spring.security.oauth2.client` auto-configuration. Remove any manual Okta client registration if present.

### 9. RedirectToFrontend

Ensure it works with OidcUser from any provider (it already uses `authentication.principal as OidcUser`). No change if generic.

## Validate

```bash
cd /Users/yona/dev/skybridge/osmt
sdk env 2>/dev/null || true
mvn clean compile -pl api -am
```

Ensure no compilation errors. Run `mvn test -pl api` to verify existing tests pass.
