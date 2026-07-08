# OSMT Authentication

This document describes how OSMT's authentication system works and how to
configure it for common deployments: single-auth (local dev), Okta OAuth2,
Google OAuth2, and staging (Google + single-auth).

## Overview

OSMT supports three authentication modes:

| Mode          | Profiles                | Use case                                   |
|---------------|-------------------------|--------------------------------------------|
| Single-auth   | `single-auth`           | Local development, testing, CI             |
| OAuth2        | `oauth2`                | Production (Okta, Google, or custom)       |
| Staging       | `oauth2,single-auth`    | Staging with both OAuth and admin fallback |

Profile selection is **automatic** based on environment variables. When running
locally via `./osmt_cli.sh -s`, the CLI detects which profiles to use. When
running in Docker, the entrypoint script (`api/docker/bin/docker_entrypoint.sh`)
determines profiles from the `ENVIRONMENT` variable and OAuth credentials.

## How It Works

### Profile Detection

1. **Explicit override**: If `OSMT_SECURITY_PROFILE` is set, it is used as-is.
2. **OAuth credentials**: If Okta or Google OAuth vars are present and valid,
   the `oauth2` profile is added.
3. **Fallback**: If no OAuth credentials are provided, `single-auth` is used.
4. **Staging**: When OAuth is configured and `ENABLE_SINGLE_AUTH=true`, the
   `single-auth` profile is added alongside OAuth so the login page shows both
   options.

### Security Configurations

- **SecurityConfig** (`@Profile("oauth2")`): Handles OAuth2 login. When
  `app.singleAuthEnabled` is true, it also adds the AdminUserAuthenticationFilter
  so form-based admin login works alongside OAuth.
- **SingleAuthSecurityConfig** (`@Profile("single-auth & !oauth2")`): Used when
  *only* single-auth is active (no OAuth). Excludes when oauth2 is active so
  SecurityConfig handles staging (oauth2,single-auth).

### Whitelabel API

The backend exposes auth configuration to the frontend via `/whitelabel/whitelabel.json`:

- `authProviders`: List of OAuth providers with `id`, `name`, `authorizationUrl`
- `singleAuthEnabled`: Whether the admin username/password form is shown
- `authMode`: `oauth2` or `single-auth`

The login page uses this to render OAuth buttons and/or the single-auth form.

---

## Single-Auth (Local Development)

**⚠️ SECURITY**: Single-auth is for local development and testing only. Do not use
in production or any environment exposed to the internet.

### When It’s Used

- OAuth credentials are missing or left as `xxxxxx`
- No `OAUTH_ISSUER`, `OAUTH_CLIENTID`, `OAUTH_CLIENTSECRET`, `OAUTH_AUDIENCE`
- No `OAUTH_GOOGLE_CLIENT_ID`, `OAUTH_GOOGLE_CLIENT_SECRET`

### Configuration

1. Initialize env files: `./osmt_cli.sh -i`
2. Leave OAuth values as `xxxxxx` or omit them
3. Use the `dev` profile (provides `APP_SESSION_TOKEN_SECRET` placeholder) or set it explicitly
4. Start the app: `./osmt_cli.sh -s`

Admin credentials (optional; defaults to `admin`/`admin`):

| Variable                     | Property                         | Default |
|------------------------------|----------------------------------|---------|
| `SINGLE_AUTH_ADMIN_USERNAME` | `app.single-auth.admin-username` | `admin` |
| `SINGLE_AUTH_ADMIN_PASSWORD` | `app.single-auth.admin-password` | `admin` |

### Authenticating

**UI**: Visit the login page and enter username/password.

**API (Basic Auth)**:

```bash
curl -u admin:admin http://localhost:8080/api/v3/skills
```

**API (Bearer token)**:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'

# Use the returned token (signed JWT, validated on each request):
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/v3/skills
```

---

## Okta OAuth2

### Prerequisites

- [Okta Developer Account](https://developer.okta.com/signup)
- OIDC Web Application in Okta

### Okta Setup

1. Create an application: OIDC, Web Application
2. Sign-in redirect URI: `{baseUrl}/login/oauth2/code/okta`  
   Example: `http://localhost:8080/login/oauth2/code/okta` for local dev
3. Copy Client ID and Client Secret
4. In Sign-On → OpenID Connect ID Token: set Issuer and Audience
5. Assign the application to users

### Environment Variables

| Variable             | Description                                                  |
|----------------------|--------------------------------------------------------------|
| `OAUTH_ISSUER`       | Okta issuer (e.g. `https://dev-xxx.okta.com/oauth2/default`) |
| `OAUTH_CLIENTID`     | Okta Client ID                                               |
| `OAUTH_CLIENTSECRET` | Okta Client Secret                                           |
| `OAUTH_AUDIENCE`     | Okta audience                                                |

### Configuration

1. Add values to `api/osmt-dev-stack.env` (copy from `api/osmt-dev-stack.env.example`)
2. Ensure redirect URI in Okta matches your `app.baseUrl` (e.g. `http://localhost:8080` for dev)
3. Start: `./osmt_cli.sh -s`

### Role Mapping

Okta uses `groups-claim=roles` in `application-oauth2.properties`. The claim
name is configurable via `app.oauth2.rolesClaim` (default `roles`). Ensure Okta
groups map to OSMT roles (e.g. `ROLE_Osmt_Admin`).

### Session Token (OAuth2)

The backend issues its own JWT after OAuth2 login instead of passing the IdP
token to the frontend. These settings control that token:

| Variable                      | Description                                          | Default        |
|-------------------------------|------------------------------------------------------|----------------|
| `APP_SESSION_TOKEN_SECRET`    | Base64-encoded secret for signing (min 256 bits).    | Required       |
| `APP_SESSION_TOKEN_EXPIRY_SECONDS` | Session validity in seconds.                   | 86400 (24h)    |
| `APP_SESSION_TOKEN_ISSUER`    | JWT issuer claim.                                    | `app.baseUrl`  |

`APP_SESSION_TOKEN_SECRET` is required. The `dev` profile provides a placeholder
for local development. In production, set it to a secure random value (e.g.
`openssl rand -base64 32`).

---

## Google OAuth2

### Prerequisites

- [Google Cloud Console](https://console.cloud.google.com/) project
- OAuth 2.0 Client ID (Web application)

### Google Setup

1. APIs & Services → Credentials → Create Credentials → OAuth client ID
2. Application type: Web application
3. Authorized redirect URI: `{baseUrl}/login/oauth2/code/google`  
   Example: `http://localhost:8080/login/oauth2/code/google` for local dev
4. Copy Client ID and Client Secret

### Environment Variables

| Variable                     | Description                                       |
|------------------------------|---------------------------------------------------|
| `OAUTH_GOOGLE_CLIENT_ID`     | Client ID (e.g. `xxx.apps.googleusercontent.com`) |
| `OAUTH_GOOGLE_CLIENT_SECRET` | Client Secret                                     |

### Configuration

1. Add values to `api/osmt-dev-stack.env`
2. Start: `./osmt_cli.sh -s`

### Role Mapping

Google OIDC typically does not include groups/roles. Options:

- **Simple auth**: Set `app.enableRoles=false` so any authenticated user can access
  endpoints.
- **Custom claims**: Configure Google Workspace or a custom IdP to add role claims,
  then set `app.oauth2.rolesClaim` to the claim name.

---

## Staging: Google + Single-Auth

Use when you want both Google OAuth and an admin fallback on the same login page
(e.g. for staging).

### Configuration

1. Set `APP_SESSION_TOKEN_SECRET` (required) or use the `dev` profile for local staging.
2. Set Google OAuth vars:
    - `OAUTH_GOOGLE_CLIENT_ID`
    - `OAUTH_GOOGLE_CLIENT_SECRET`
3. Set `ENABLE_SINGLE_AUTH=true`
4. Set admin credentials:
    - `SINGLE_AUTH_ADMIN_USERNAME`
    - `SINGLE_AUTH_ADMIN_PASSWORD`

### Example

Copy `api/osmt-staging.env.example` to `api/osmt-staging.env` and fill in values.
For Docker, set `ENVIRONMENT` to include `oauth2,single-auth` (or let the
entrypoint add them from the env vars above).

### Login Page

The login page will show:

- “Sign in with Google” button
- “Or sign in with an OSMT username” with the admin form

---

## Environment Files

| File                             | Purpose                                  |
|----------------------------------|------------------------------------------|
| `api/osmt-dev-stack.env.example` | Local dev (single-auth, Okta, or Google) |
| `api/osmt-staging.env.example`   | Staging (Google + single-auth)           |
| `test/osmt-apitest.env.example`  | API tests                                |

Create env files with `./osmt_cli.sh -i`. Never commit secrets; `osmt*.env` files
are gitignored.

---

## Login Flow

### OAuth-Only (Okta or Google)

1. User goes to `/login`
2. If exactly one OAuth provider and no single-auth: redirect to provider
3. Otherwise: show OAuth buttons
4. User clicks provider → OAuth redirect → callback → session created
5. User is redirected to app

### Single-Auth Only

1. User goes to `/login`
2. Form is shown
3. User submits username/password → POST to `/api/auth/login`
4. Backend validates credentials → returns JWT
5. Frontend stores token and navigates to app

### Staging (OAuth + Single-Auth)

1. User goes to `/login`
2. OAuth buttons and admin form are shown
3. User chooses OAuth or form login
4. Same flows as above for whichever option is used

---

## Known Issues

### Token in URL (OAuth2 redirect)

After OAuth2 login, the backend redirects to the frontend with the session token
in the query string (`?token=...`). Tokens in URLs may be exposed via:

- HTTP Referer header
- Server access logs
- Browser history

This behavior predates the backend-issued session token work. Mitigations (e.g.
using fragment, or code exchange) may be considered in future.

---

## Related Files

- `api/src/main/resources/config/application-single-auth.properties` – single-auth config
- `api/src/main/resources/config/application-oauth2.properties` – OAuth2 (Okta, Google, custom)
- `api/docker/bin/docker_entrypoint.sh` – Docker profile logic
- `bin/lib/common.sh` – `detect_security_profile()` for local dev
- [README.md](../README.md) – Quick start and OAuth setup
