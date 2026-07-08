# Phase 3: Create SessionTokenService

## Scope of phase

Create a service that builds a signed JWT from OidcUser. Extract sub, email, name, and roles from authentication authorities. Set issuer, expiration, issued-at.

## Code Organization Reminders

- Place abstract/general logic first.
- Helper functions at the bottom.

## Implementation Details

### 1. Create SessionTokenService.kt

Location: `api/src/main/kotlin/edu/wgu/osmt/security/SessionTokenService.kt`

```kotlin
@Service
@Profile("oauth2")
class SessionTokenService(
    private val appConfig: AppConfig,
    private val jwtEncoder: JwtEncoder,
) {
    fun createToken(oidcUser: OidcUser): String {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(appConfig.sessionTokenExpirySeconds)
        val roles = oidcUser.authorities
            .map { it.authority }
            .filter { it.startsWith("ROLE_") }
            .joinToString(",")

        val claims = JwtClaimsSet.builder()
            .issuer(appConfig.sessionTokenIssuer)
            .subject(oidcUser.subject ?: oidcUser.name)
            .claim("email", oidcUser.email ?: oidcUser.name)
            .claim("name", oidcUser.fullName ?: oidcUser.name)
            .claim("roles", roles.ifEmpty { appConfig.roleView })  // fallback if no roles
            .issuedAt(now)
            .expiresAt(expiresAt)
            .build()

        val params = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
        return jwtEncoder.encode(params).tokenValue
    }
}
```

Injection: Use `@Qualifier` if multiple JwtEncoder beans – name the session encoder bean or use `sessionTokenJwtEncoder` as the method name for bean id.

### 2. Inject the correct JwtEncoder

Ensure SessionTokenService injects `sessionTokenJwtEncoder`:

```kotlin
constructor(
    private val appConfig: AppConfig,
    @Qualifier("sessionTokenJwtEncoder") private val jwtEncoder: JwtEncoder,
)
```

Or use `@Bean` name explicitly in SessionTokenJwtConfig.

### 3. Fallback role

When OAuth2 user has no roles (e.g. Google without custom claims), use `appConfig.roleView` or configurable default so frontend gets a non-null role. Align with `app.enableRoles` behavior if needed.

## Validate

```bash
cd api && mvn compile -q
```
