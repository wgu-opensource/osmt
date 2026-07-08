# Phase 7: Simplify Frontend storeToken

## Scope of phase

Revert storeToken to only read the `roles` claim from the JWT. Remove fallbacks for `groups` and `authorities`. Update auth-service.spec.ts to remove tests for groups/authorities extraction.

## Code Organization Reminders

- Keep storeToken logic minimal.

## Implementation Details

### 1. Update auth-service.ts storeToken

Restore to reading only `decoded.roles`:

```typescript
storeToken(accessToken: string): void {
  localStorage.setItem(STORAGE_KEY_TOKEN, accessToken);
  try {
    const decoded = JSON.parse(atob(accessToken.split('.')[1]));
    if (decoded?.roles) {
      localStorage.setItem(STORAGE_KEY_ROLE, decoded.roles);
    }
  } catch (e) {
    // Token may not be a JWT, ignore
  }
}
```

Handle array: if `decoded.roles` is an array, join to string. Backend sends comma-separated string. Frontend expects string for getRole(). So: `const roles = decoded?.roles; if (roles !== undefined) { const value = Array.isArray(roles) ? roles.join(',') : String(roles); if (value) localStorage.setItem(STORAGE_KEY_ROLE, value); }`

### 2. Update auth-service.spec.ts

Remove tests:
- `storeToken should extract role from groups claim (OAuth2)`
- `storeToken should extract role from authorities array`

Keep or add test for `roles` claim (string and array).

### 3. Restore beforeEach if needed

If we added localStorage cleanup for ROLE/TOKEN in beforeEach, keep it for tests that need clean state.

## Validate

```bash
cd ui && npm run lint && npx ng test --include='**/auth-service.spec.ts' --browsers=ChromeHeadless --watch=false
```
