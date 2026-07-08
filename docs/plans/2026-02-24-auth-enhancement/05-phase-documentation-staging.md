# Phase 5: Documentation and Staging Example

## Scope of phase

Add example env/config for Google + single-auth staging. Update README. Document role mapping for Google.

## Code Organization Reminders

- Keep examples concise; use placeholders for secrets
- Document in README and api/README as appropriate

## Implementation Details

### 1. Create osmt-staging.env.example

Create `api/osmt-staging.env.example` (or add to existing example):

```bash
# Staging: Google OAuth2 + Single-Auth
# Both options presented on login page

# Google OAuth2
OAUTH_GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
OAUTH_GOOGLE_CLIENT_SECRET=your-google-client-secret

# Single-auth (admin fallback)
SINGLE_AUTH_ADMIN_USERNAME=admin
SINGLE_AUTH_ADMIN_PASSWORD=secure-password

# Profiles: oauth2,apiserver,single-auth
# (or equivalent for your deployment)
```

### 2. Update application-oauth2-google.properties

Ensure it uses correct env vars. Add to profile activation docs.

### 3. Update README.md

- Update OAuth2 section: explain standard Spring config for Google, Okta
- Add "Staging with Google + Single-Auth" subsection:
  - Set OAUTH_GOOGLE_CLIENT_ID, OAUTH_GOOGLE_CLIENT_SECRET
  - Enable single-auth profile
  - Configure Google Cloud Console redirect URI: `{baseUrl}/login/oauth2/code/google`
- Document app.oauth2.rolesClaim for custom providers
- Add "Role Mapping for Google" note: Google OIDC does not include groups by default; use enableRoles=false or configure custom claims in GCP/Workspace

### 4. Update api/README.md

- Update profile table: oauth2 (replaces oauth2-okta), single-auth
- Document spring.security.oauth2.client.registration.* properties
- Link to Google Cloud setup for OAuth credentials

### 5. Update osmt-dev-stack.env.example

Add OAUTH_GOOGLE_* vars as optional. Update comments for multi-provider.

## Validate

```bash
cd /Users/yona/dev/skybridge/osmt
# Verify examples exist and README links work
grep -r "oauth2" README.md api/README.md
```
