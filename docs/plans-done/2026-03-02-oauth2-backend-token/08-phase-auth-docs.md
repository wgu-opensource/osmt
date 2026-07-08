# Phase 8: Document Auth Env Vars

## Scope of phase

Add session token environment variables to docs/auth.md and env example files. Include APP_SESSION_TOKEN_SECRET, APP_SESSION_TOKEN_EXPIRY_SECONDS, APP_SESSION_TOKEN_ISSUER.

## Code Organization Reminders

- Place new vars in logical sections.
- Keep doc consistent with existing env var style.

## Implementation Details

### 1. Update docs/auth.md

Add a section "Session Token (OAuth2)" under Okta OAuth2 or as a subsection of OAuth2:

```markdown
### Session Token Configuration

When using OAuth2, the backend issues its own JWT after successful IdP login. These settings control that token:

| Variable | Description | Default |
|----------|-------------|---------|
| `APP_SESSION_TOKEN_SECRET` | Base64-encoded secret for signing (min 256 bits). Required in production. | Derived from baseUrl in dev |
| `APP_SESSION_TOKEN_EXPIRY_SECONDS` | Session validity in seconds. | 86400 (24 hours) |
| `APP_SESSION_TOKEN_ISSUER` | JWT issuer claim. | `app.baseUrl` |
```

### 2. Update api/osmt-dev-stack.env.example

Add the three vars with comments explaining when to set them.

### 3. Update test/osmt-apitest.env.example if it documents auth

Add session token vars if the test env example documents OAuth2 configuration.

## Validate

```bash
# No code changes; manual review of docs
```
