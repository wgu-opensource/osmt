# Phase 2: Create SessionTokenJwtConfig

## Scope of phase

Create a configuration class that provides JwtEncoder and JwtDecoder beans for the oauth2 profile. Use HS256 with a secret key. Decoder validates our issuer.

## Code Organization Reminders

- One concept per file.
- Place configuration beans before helper logic.

## Implementation Details

### 1. Create SessionTokenJwtConfig.kt

Location: `api/src/main/kotlin/edu/wgu/osmt/security/SessionTokenJwtConfig.kt`

Use Spring Security 6 JWT APIs from `spring-boot-starter-oauth2-resource-server`:
- `NimbusJwtEncoder` with `MacAlgorithm.HS256` and secret key
- `NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build()`
- `JwtValidators.createDefaultWithIssuer(issuer)` for issuer validation

Provide two beans:
1. `sessionTokenJwtEncoder()` – for SessionTokenService to create JWTs
2. `sessionTokenJwtDecoder()` – validates our tokens
3. `jwtDecoder()` with `@Profile("oauth2 & !single-auth")` – returns sessionTokenJwtDecoder for oauth2-only (resource server consumes this)

Secret resolution: Base64-decode `app.sessionTokenSecret` if set; else SHA-256 of `baseUrl` for dev fallback.

### 2. Handle empty secret in production

If `sessionTokenSecret` is empty in production (oauth2), log a warning. The fallback is only for local dev. Consider failing fast when oauth2 and secret empty and baseUrl is production-like.

### 3. Use JwtEncoderParameters

Check Spring Security 6 API: `JwtEncoder.encode(JwtEncoderParameters)` – parameters include claims. SessionTokenService will use this in the next phase.

## Validate

```bash
cd api && mvn compile -q
```
