# OAuth2 Backend-Issued Token

## Scope of Work

Replace the current OAuth2 flow (pass-through of IdP token to frontend) with a backend-issued session token. After OAuth2 success, the backend creates its own JWT with extracted roles and passes that to the frontend instead of the IdP's ID token.

**Benefits:**
- Consistent token format for frontend (same `roles` claim as single-auth)
- Role/scope logic stays on backend; no IdP claim-name guessing
- Fixes OAuth2 logout bug when viewing public page in new tab
- Single token shape regardless of provider (Okta, Google, etc.)

## Current State of the Codebase

### OAuth2 Flow (api/)
- **RedirectToFrontend** (`SecurityConfig.kt`): On OAuth2 success, passes `oidcUser.idToken.tokenValue` (raw IdP token) to frontend via `?token=`
- **userAuthoritiesMapper**: Extracts roles from IdP token claims (`oauth2RolesClaim`, default `roles`); maps to `GrantedAuthority`
- **JwtDecoder** (oauth2 only): Validates Bearer tokens against OAuth2 issuer (Okta/Google); rejects tokens not from IdP
- **SingleAuthAwareJwtDecoderConfig** (oauth2 & single-auth): Accepts `admin-jwt-*` tokens; delegates others to IdP decoder

### Single-Auth Flow (api/)
- **AdminAuthController**: Creates backend JWT with `.claim("roles", ADMIN_ROLE)`; returns in API response
- **AdminUserAuthenticationFilter** / **SingleAuthAwareJwtDecoderConfig**: Accepts `admin-jwt-*` tokens; builds Jwt in-memory (no signature validation)

### Frontend (ui/)
- **LoginSuccessComponent**: Reads `?token=` from URL, calls `authService.storeToken(token)`
- **AuthService.storeToken**: Decodes JWT, extracts `roles` (and `groups`/`authorities` fallback); stores in localStorage
- **validateRoles** (AppComponent): Redirects to /logout if token exists but role is null

### Token Format Mismatch
- IdP tokens (Okta, Google) use provider-specific claim names (`groups`, custom claims)
- Frontend tries to guess; fails when structure differs
- Backend has correct role info in `authentication.authorities` after OAuth2

## Questions That Need Answers

### Q1: Session Token Signing
**Context**: Backend-issued tokens should be verifiable. Options: (A) Signed JWT with symmetric key (HS256) – standard, stateless; (B) Opaque token + server-side session – requires Redis/memory store.

**Answer**: (A) Signed JWT with HS256. Use Spring's JwtEncoder with a configurable secret key. Stateless, no session store.

### Q2: Token Expiry
**Context**: Single-auth uses 3600s (1 hour). Session token expiry controls how long a user stays logged in before re-authenticating.

**Answer**: Default 24 hours (86400 seconds). Configurable via env var (e.g. `APP_SESSION_TOKEN_EXPIRY_SECONDS`). Add to `docs/auth.md` and env example files.

### Q3: JwtDecoder When OAuth2-Only (No Single-Auth)
**Context**: When `oauth2` profile is active without `single-auth`, the default OAuth2 resource server JwtDecoder validates only IdP tokens. We need it to also accept our session tokens.

**Answer**: (B) Only accept our session tokens. Replace IdP decoder for API requests. Frontend and Postman both get our token from redirect. Simpler; no delegation to IdP decoder.

### Q4: Token Prefix / Issuer
**Context**: Need a consistent issuer for our signed JWTs so the decoder validates correctly.

**Answer**: (C) Use `app.sessionTokenIssuer` config, defaulting to `app.baseUrl`. Allows override per deployment; aligns with JWT issuer conventions.

### Q5: Frontend storeToken Simplification
**Context**: Once backend always sends token with `roles` claim, frontend fallbacks (groups, authorities) may be unnecessary.

**Answer**: (B) Simplify immediately – only read `roles` claim. Same as single-auth. Reduces edge-case risk.
