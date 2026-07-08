# OAuth2 Backend-Issued Token – Design

## Scope of Work

Replace OAuth2 pass-through of IdP token with backend-issued signed JWT. After OAuth2 success, backend creates its own JWT (roles from `authentication.authorities`), redirects with `?token=<our-jwt>`. Frontend receives consistent token format; scope/role logic stays on backend.

## File Structure

```
api/src/main/kotlin/edu/wgu/osmt/
├── config/
│   └── AppConfig.kt                      # UPDATE: Add sessionTokenSecret, sessionTokenExpirySeconds, sessionTokenIssuer
├── security/
│   ├── SessionTokenService.kt            # NEW: Creates signed JWT from OidcUser (roles, sub, email, etc.)
│   ├── SessionTokenJwtConfig.kt         # NEW: JwtEncoder + JwtDecoder beans for oauth2 profile; uses our issuer/secret
│   ├── SecurityConfig.kt                # UPDATE: Wire SessionTokenService into RedirectToFrontend; oauth2ResourceServer uses our decoder
│   ├── RedirectToFrontend.kt            # (in SecurityConfig.kt) UPDATE: Use SessionTokenService.createToken(oidcUser) instead of idToken.tokenValue
│   └── SingleAuthAwareJwtDecoderConfig.kt # UPDATE: When oauth2 & single-auth, accept admin-jwt-* AND our session tokens

api/src/main/resources/config/
├── application.properties               # UPDATE: Add app.sessionTokenSecret, app.sessionTokenExpirySeconds, app.sessionTokenIssuer
└── application-oauth2.properties        # UPDATE: Session token config (if needed)

docs/
└── auth.md                              # UPDATE: Document APP_SESSION_TOKEN_* env vars

api/osmt-dev-stack.env.example            # UPDATE: Add APP_SESSION_TOKEN_EXPIRY_SECONDS, APP_SESSION_TOKEN_SECRET
```

## Conceptual Architecture

```
                    OAuth2 Login Flow (unchanged until callback)
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Browser   │────▶│   Okta/     │────▶│   Spring    │────▶│  Redirect   │
│  (redirect) │     │   Google    │     │  OAuth2     │     │ ToFrontend  │
└─────────────┘     └─────────────┘     │  Callback   │     └──────┬──────┘
                                       └─────────────┘              │
                                                                    │ NEW: SessionTokenService
                                                                    │      .createToken(oidcUser)
                                                                    │      → signed JWT (roles, sub, email)
                                                                    ▼
                    ┌─────────────────────────────────────────────────────────────┐
                    │  Redirect: {frontendUrl}/login/success?token=<our-signed-jwt> │
                    └─────────────────────────────────────────────────────────────┘
                                                                    │
                                                                    ▼
┌─────────────┐     ┌─────────────┐
│   Frontend  │     │  API       │
│  storeToken │────▶│  Bearer    │────▶  SessionTokenJwtConfig.JwtDecoder
│  (roles)    │     │  <our-jwt> │       validates with our secret + issuer
└─────────────┘     └─────────────┘       → Jwt with roles claim
```

**Token flow:**
1. OAuth2 callback produces `OidcUser` with `authentication.authorities` (roles from userAuthoritiesMapper).
2. `RedirectToFrontend` calls `SessionTokenService.createToken(oidcUser)` → signed JWT with `sub`, `email`, `name`, `roles`, `iss`, `exp`.
3. Redirect to frontend with `?token=<jwt>`.
4. Frontend `storeToken` decodes and reads `roles` (only).
5. API requests: `Authorization: Bearer <our-jwt>`. `SessionTokenJwtConfig` provides `JwtDecoder` that validates our token (issuer + signature). No IdP validation.

**SessionTokenJwtConfig** (oauth2 profile):
- `JwtEncoder`: NimbusJwtEncoder with MacAlgorithm.HS256, secret from `app.sessionTokenSecret`.
- `JwtDecoder`: NimbusJwtDecoder.withMacAlgorithm(secret).build(); set expected issuer.
- Overrides Spring Boot's default OAuth2 resource server JwtDecoder (which validates IdP issuer). Our decoder only accepts our tokens.

**Staging (oauth2 + single-auth):**
- Need to accept both `admin-jwt-*` (single-auth) and our session tokens.
- Extend `SingleAuthAwareJwtDecoderConfig` or create composite decoder: if token starts with `admin-jwt-`, use in-memory Jwt; else use our session JwtDecoder.

## Main Components

| Component | Responsibility |
|-----------|----------------|
| **SessionTokenService** | `createToken(oidcUser): String` – builds JWT claims from OidcUser, encodes with JwtEncoder, returns compact string |
| **SessionTokenJwtConfig** | Provides JwtEncoder and JwtDecoder beans for oauth2 profile; configures HS256 secret and issuer |
| **RedirectToFrontend** | Calls SessionTokenService instead of passing idToken.tokenValue |
| **SingleAuthAwareJwtDecoderConfig** | When staging: accept admin-jwt-* OR our session token; delegate to session decoder for non-admin tokens |

## Config Additions

- `app.sessionTokenSecret` – Base64-encoded secret for HS256 (env: `APP_SESSION_TOKEN_SECRET`). If unset in dev, generate/derive from baseUrl.
- `app.sessionTokenExpirySeconds` – Default 86400 (24h). Env: `APP_SESSION_TOKEN_EXPIRY_SECONDS`.
- `app.sessionTokenIssuer` – Default `app.baseUrl`. Env: `APP_SESSION_TOKEN_ISSUER`.
