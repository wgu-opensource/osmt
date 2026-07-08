# Phase 9: Cleanup & Validation

## Scope of phase

Remove temporary code, fix warnings, run full test suite. Add plan summary. Validate end-to-end OAuth2 flow.

## Code Organization Reminders

- Grep for TODO, FIXME, debug prints.
- Fix all linter issues.

## Implementation Details

### 1. Grep for temporary code

```bash
git diff | grep -E 'TODO|FIXME|console\.log|debugger'
```

Remove or resolve any found.

### 2. Run full validation

```bash
cd /Users/yona/dev/skybridge/osmt
sdk env 2>/dev/null || true
mvn clean test -q
cd ui && npm run lint
cd ui && npx ng test --browsers=ChromeHeadless --watch=false
```

### 3. Manual verification

- Start API with oauth2 profile (or staging).
- Complete OAuth2 login.
- Verify redirect to /login/success?token=... and token is a JWT (three base64 parts).
- Decode token; verify `roles` and `iss` claims.
- Call a protected API with Bearer token; verify 200.
- Open public skill page in new tab while logged in; verify no redirect to /logout.

### 4. Plan summary

Add `summary.md` to plan directory with bullet list of completed work.

### 5. Plan cleanup (per plan process)

Move plan files to `docs/plans-done/` when implementation is complete.

### 6. Commit

```
feat(auth): issue backend JWT for OAuth2 instead of passing IdP token

- Add SessionTokenService to create signed JWT from OidcUser
- Add SessionTokenJwtConfig (JwtEncoder/Decoder, HS256)
- RedirectToFrontend passes our token instead of IdP token
- JwtDecoder validates only our tokens (oauth2); staging accepts admin-jwt-* and session token
- Session token: 24h default, configurable via APP_SESSION_TOKEN_*
- Simplify frontend storeToken to roles claim only
- Document session token env vars in auth.md
```

## Validate

```bash
mvn clean test -q && cd ui && npm run lint && npx ng test --browsers=ChromeHeadless --watch=false
```
