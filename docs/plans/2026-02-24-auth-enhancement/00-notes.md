# Auth Enhancement: Arbitrary OAuth2 Providers and Multiple Auth Modes

## Scope of Work

1. **Arbitrary OAuth2 providers** – Support any OIDC/OAuth2 provider (not just Okta), including Google
2. **Multiple configured auth providers** – Allow both single-auth and OAuth2 to be configured simultaneously (e.g., staging)
3. **Login page restyling** – Update login page to match the rest of the app’s design system
4. **Staging scenario** – single-auth and OAuth2 with Google configured together

## Current State of the Codebase

### Auth Modes (Mutually Exclusive)
- **OAuth2 (oauth2-okta profile)** – Okta only, via `okta-spring-boot-starter`
- **Single-auth (single-auth profile)** – Admin username/password, dev/test only
- Mode is chosen by env vars: OAuth vars present → oauth2-okta; otherwise → single-auth
- Only one profile/security config is active at a time

### Backend
- `SecurityConfig.kt` – OAuth2 + JWT resource server; `@Profile("oauth2-okta | OTHER-OAUTH-PROFILE")`
- `SingleAuthSecurityConfig.kt` – `@Profile("single-auth")`; AdminUserAuthenticationFilter
- `application-oauth2-okta.properties` – `okta.oauth2.*` (Okta-specific format)
- Spring Boot OAuth2 uses `okta.oauth2.*`; standard Spring uses `spring.security.oauth2.client.registration.*`
- OAuthHelper – extracts user/roles from Jwt or OAuth2User; Okta uses `groupsClaim=roles`

### Frontend
- `LoginComponent` – if `authMode === 'single-auth'` show form; else redirect to `loginUrl`
- `loginUrl` – hardcoded `/oauth2/authorization/okta` in envs; single-auth overrides via whitelabel to `/login`
- Whitelabel `/whitelabel/whitelabel.json` – provides `loginUrl`, `authMode` at runtime
- `IAppConfig` – single `loginUrl`, single `authMode`

### Login Page Styling
- `login.component.html` – form with m-button, m-text; OAuth2 mode shows loader (redirect)
- `login.component.scss` – gradient background, centered card; uses `--color-*` CSS vars
- App uses: `m-navBar`, `m-button`, `m-text`, `app-formfield`, `m-quickLinks`

## Questions That Need Answers

### Q1: Staging “single-auth and oauth2 with Google” – Coexistence Model
**Context**: You want staging to have single-auth and OAuth2 (Google) configured together.

**Options**:
- (A) Both options on the login page – user chooses “Sign in with Google” or “Admin login” (username/password)
- (B) Primary provider – one method is primary (e.g., auto-redirect to Google); single-auth available via a separate link/section
- (C) Other – e.g., different paths for different user types

**Answer**: (A) – Show both options on the login page when both are configured. Users choose their preferred method.

---

### Q2: Multiple OAuth2 Providers (Future)
**Context**: With arbitrary providers, we may have multiple OAuth providers (e.g., Okta + Google).

**Options**:
- (A) Provider picker – login page shows buttons for each provider when multiple are configured
- (B) Single OAuth provider only – one OAuth provider at a time; multiple providers not in scope for this plan
- (C) Primary OAuth provider – one primary that auto-redirects; others via “Other sign-in options”

**Answer**: (A) – Provider picker when multiple OAuth providers are configured. Shows a button for each provider.

---

### Q3: Okta Backward Compatibility
**Context**: Current setup uses Okta Spring Boot starter with `okta.oauth2.*` config.

**Options**:
- (A) Migrate fully – switch to `spring.security.oauth2.client.registration` for all providers including Okta; drop Okta starter
- (B) Keep Okta path – retain Okta starter for `okta` registration; add generic config for other providers
- (C) Support both – accept both Okta config and generic config; Okta starter optional

**Answer**: (A) – Migrate fully to standard Spring OAuth2 config. Drop Okta starter. Use `spring.security.oauth2.client.registration` for all providers including Okta.

---

### Q4: Role Mapping for Google
**Context**: Okta uses `groupsClaim=roles` for role mapping. Google OIDC does not provide groups by default.

**Options**:
- (A) Email domain / allowlist – map specific Google accounts (e.g., your org domain) to admin
- (B) No roles from Google – use `enableRoles=false` or assign a default role to all OAuth users
- (C) Custom claim – configure Google to add a custom claim (requires GCP/Workspace Admin)
- (D) Per-deployment config – make role mapping configurable (claim name, mapping rules)

**Answer**: (D) – Configurable role mapping per deployment (claim name, mapping rules). Document Google without custom claims: use `enableRoles=false` or default role.

---

### Q5: Login Page Style Alignment
**Context**: Login page should match the rest of the app.

**Current**: Centered card on gradient; `m-button`, `m-text`; OAuth2 shows loader.

**Suggested direction**: Use the app shell (header/nav structure) and design tokens (`--color-*`, `m-button`, `m-text`, `m-navBar` branding). Align with other forms (e.g., `form-field-text`, `form-field-submit`). Avoid full-page gradient; use standard layout with a content area. When multiple providers, show provider buttons in a consistent layout.

**Answer**: Use skill form as reference for layout and component patterns.

---

## Notes

### Q1 Answer
- Both single-auth and OAuth2 options shown on login page when both configured

### Q2 Answer
- Provider picker when multiple OAuth providers configured; button per provider

### Q3 Answer
- Migrate fully to standard Spring OAuth2; drop Okta starter

### Q4 Answer
- Configurable role mapping per deployment; document Google without custom claims

### Q5 Answer
- Use skill form as reference for login page layout and component patterns
