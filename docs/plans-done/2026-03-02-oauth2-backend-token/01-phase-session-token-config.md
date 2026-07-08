# Phase 1: Add Session Token Config

## Scope of phase

Add configuration properties for session token: secret, expiry, and issuer. Wire into AppConfig and application properties. Update env example files.

## Code Organization Reminders

- Prefer one concept per file.
- Place config keys in logical groups.
- Keep env var names consistent (APP_SESSION_TOKEN_*).

## Implementation Details

### 1. Add to AppConfig.kt

```kotlin
@Value("\${app.sessionTokenSecret:}")
val sessionTokenSecret: String,
@Value("\${app.sessionTokenExpirySeconds:86400}")
val sessionTokenExpirySeconds: Long,
@Value("\${app.sessionTokenIssuer:${app.baseUrl}}")
val sessionTokenIssuer: String,
```

Note: `sessionTokenIssuer` default to baseUrl may need separate handling; use `@Value("\${app.sessionTokenIssuer:\${app.baseUrl}}")` or define default in properties.

### 2. Add to application.properties

```properties
# Session token (OAuth2 backend-issued JWT)
app.sessionTokenSecret=${APP_SESSION_TOKEN_SECRET:}
app.sessionTokenExpirySeconds=${APP_SESSION_TOKEN_EXPIRY_SECONDS:86400}
app.sessionTokenIssuer=${APP_SESSION_TOKEN_ISSUER:${app.baseUrl}}
```

### 3. Add to application-oauth2.properties (if profile-specific)

Session token is used when oauth2 is active. The main application.properties can hold defaults. Ensure oauth2 profile loads these.

### 4. Update api/osmt-dev-stack.env.example

Add:
```
# Session token (OAuth2 only) - optional; defaults used if unset
# APP_SESSION_TOKEN_SECRET=base64-encoded-secret-at-least-256-bits
# APP_SESSION_TOKEN_EXPIRY_SECONDS=86400
# APP_SESSION_TOKEN_ISSUER=https://your-domain.com
```

### 5. Update api/osmt-dev-stack.env (or equivalent) if it exists

Add commented placeholders for the new vars.

## Validate

```bash
cd api && mvn compile -q
```

Ensure AppConfig compiles. If `sessionTokenIssuer` default causes issues (circular ref), use a simple default like empty string or "osmt" and resolve in SessionTokenService.
