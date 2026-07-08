# Design: Auth Enhancement – Arbitrary OAuth2 Providers and Multiple Auth Modes

## Scope of Work

1. **Arbitrary OAuth2 providers** – Support any OIDC/OAuth2 provider (Google, Okta, etc.) via standard Spring OAuth2 config
2. **Multiple configured auth providers** – Allow single-auth and OAuth2 configured simultaneously; provider picker when multiple OAuth providers
3. **Login page restyling** – Match skill form layout and component patterns
4. **Staging scenario** – single-auth + OAuth2 (Google) both available on login page

## File Structure

```
api/
├── pom.xml                                    # UPDATE: Remove okta-spring-boot-starter
├── src/main/
│   ├── kotlin/edu/wgu/osmt/
│   │   ├── config/
│   │   │   └── AppConfig.kt                   # UPDATE: Add authProviders, rolesClaim
│   │   └── security/
│   │       ├── SecurityConfig.kt             # UPDATE: Unified config; oauth2 + single-auth
│   │       ├── SingleAuthSecurityConfig.kt   # UPDATE or REMOVE: Merge into unified
│   │       ├── OAuthHelper.kt                # UPDATE: Configurable roles claim
│   │       ├── AdminUserAuthenticationFilter.kt  # KEEP
│   │       ├── AdminAuthController.kt         # KEEP
│   │       ├── AuthConfigProvider.kt          # NEW: Expose providers to whitelabel
│   │       └── SecurityConfigHelper.kt        # KEEP
│   └── resources/config/
│       ├── application.properties             # UPDATE: Add oauth2.rolesClaim
│       ├── application-oauth2-okta.properties # REMOVE
│       └── application-oauth2.properties      # NEW: Generic OAuth2 config
ui/
├── src/app/
│   ├── auth/
│   │   ├── login.component.ts                 # UPDATE: Provider picker, multi-mode
│   │   ├── login.component.html               # UPDATE: Skill-form-style layout
│   │   ├── login.component.scss               # UPDATE: Match form styling
│   │   └── login.component.spec.ts           # UPDATE: Tests
│   └── models/
│       └── app-config.model.ts                # UPDATE: authProviders: AuthProvider[]
api/docker/bin/
└── docker_entrypoint.sh                       # UPDATE: OAuth config detection
```

## Conceptual Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Frontend (Login Page)                        │
├─────────────────────────────────────────────────────────────────────┤
│  App Shell (header, content, footer) – same as skill form            │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  m-skillBackground card (skill-form layout)                   │  │
│  │  m-iconTitle: "Sign In"                                        │  │
│  │                                                                 │  │
│  │  OAuth Providers (from whitelabel)          Single-Auth (if on) │  │
│  │  ┌─────────────────────────┐              ┌────────────────┐   │  │
│  │  │ [Sign in with Google]   │              │ Username       │   │  │
│  │  │ [Sign in with Okta]     │     or       │ Password       │   │  │
│  │  └─────────────────────────┘              │ [Login]        │   │  │
│  │                                            └────────────────┘   │  │
│  └───────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  GET /whitelabel/whitelabel.json                                     │
│  Returns: authProviders: [{id, name, authorizationUrl}],            │
│           singleAuthEnabled                                          │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     Backend Security (Unified)                        │
├─────────────────────────────────────────────────────────────────────┤
│  ClientRegistrationRepository ← spring.security.oauth2.client.*      │
│  (google, okta, etc. from properties/env)                            │
│                                                                      │
│  SecurityFilterChain:                                                 │
│    1. AdminUserAuthenticationFilter (if single-auth enabled)          │
│    2. oauth2Login() – all configured providers                       │
│    3. oauth2ResourceServer().jwt()                                   │
│    4. Shared endpoint rules (SecurityConfigHelper)                   │
└─────────────────────────────────────────────────────────────────────┘
```

## Main Components

1. **Backend OAuth2 config** – Use `spring.security.oauth2.client.registration.{id}` for each provider. Remove Okta starter. Support Google, Okta, and arbitrary providers via standard properties.

2. **Unified SecurityConfig** – Single security config that:
   - Enables OAuth2 login for all configured registrations
   - Optionally enables single-auth (AdminUserAuthenticationFilter)
   - Reuses SecurityConfigHelper for endpoint rules
   - Profile: `oauth2` (or similar) when any OAuth provider is configured

3. **AuthConfigProvider** – Reads ClientRegistrationRepository, builds list of `{id, clientName, authorizationUrl}` for whitelabel. Injected into UiController.

4. **Whitelabel API** – Extend response with `authProviders` array and `singleAuthEnabled` boolean. Frontend uses these to render provider picker and single-auth form.

5. **Login component** – Skill-form layout (l-stickySidebar, m-skillBackground, m-iconTitle). OAuth provider buttons and single-auth form driven by whitelabel. No auto-redirect when multiple options.

6. **OAuthHelper** – Configurable roles claim (`app.oauth2.rolesClaim`) with default for Okta compatibility. Document Google/custom claim setup.
