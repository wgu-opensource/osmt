# Phase 6: Cleanup and Validation

## Scope of phase

Remove temporary code, fix warnings, run full validation. Create plan summary.

## Code Organization Reminders

- No nested code more than 2 levels deep
- Remove TODO comments from development
- Follow .eslintrc.json, .prettierrc, .editorconfig

## Implementation Details

### 1. Grep for temporary artifacts

```bash
cd /Users/yona/dev/skybridge/osmt
rg "TODO|FIXME|XXX|HACK|console\.log" \
  --glob '!node_modules' --glob '!*.lock' \
  -g '!docs/plans*' 2>/dev/null || true
```

Remove or resolve any unintended TODOs in new/modified files.

### 2. Linting and formatting

```bash
cd ui && npm run format:check && npm run lint
cd api && mvn validate -q
```

### 3. Full build and test

```bash
cd /Users/yona/dev/skybridge/osmt
sdk env 2>/dev/null || true
mvn clean install -pl api -am
cd ui && npm run build-prod && npm run ci-test
```

### 4. Plan summary

Create `docs/plans/2026-02-24-auth-enhancement/summary.md` with:

- Completed work summary
- Key files changed
- Migration notes for existing deployments (Okta config env var changes if any)

### 5. Move plan to plans-done (after commit)

Per plan process, move to `docs/plans-done/` after commit.

## Validate

```bash
cd /Users/yona/dev/skybridge/osmt
sdk env 2>/dev/null || true
mvn clean install -pl api -am
cd ui && npm run format:check && npm run lint && npm run ci-test && npm run build-prod
cd ..
bin/validate-versions.sh 2>/dev/null || true
```

## Commit

```
feat(auth): support arbitrary OAuth2 providers and multiple auth modes

- Migrate from Okta starter to standard Spring OAuth2 config
- Add AuthConfigProvider and whitelabel authProviders, singleAuthEnabled
- Unify SecurityConfig for OAuth2 + single-auth coexistence
- Redesign login page with skill-form layout and provider picker
- Add staging example for Google + single-auth
- Document role mapping for Google
```
