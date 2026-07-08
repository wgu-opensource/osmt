# Phase 3: Frontend App Config and Auth Model

## Scope of phase

Add authProviders and singleAuthEnabled to IAppConfig. Update AppConfig.load() to consume new whitelabel fields.

## Code Organization Reminders

- Keep interfaces minimal; add only required fields
- Place type definitions before implementations

## Implementation Details

### 1. Add AuthProvider interface

In `ui/src/app/models/app-config.model.ts` (or new file `auth-provider.model.ts`):

```typescript
export interface AuthProvider {
  id: string;
  name: string;
  authorizationUrl: string;
}
```

### 2. Update IAppConfig

In `app-config.model.ts`:

```typescript
export interface IAppConfig {
  baseApiUrl: string;
  loginUrl: string;
  authMode?: string;
  authProviders?: AuthProvider[];
  singleAuthEnabled?: boolean;
  // ... existing fields
}
```

### 3. Update DefaultAppConfig

Add defaults:

```typescript
authProviders = [];
singleAuthEnabled = false;
```

### 4. Update AppConfig.load()

In `app.config.ts`, when merging whitelabel response:

```typescript
AppConfig.settings.authProviders =
  (value as IAppConfig).authProviders ?? [];
AppConfig.settings.singleAuthEnabled =
  (value as IAppConfig).singleAuthEnabled ?? false;
```

Preserve existing loginUrl logic for backward compatibility.

### 5. Update app.config.spec.ts

Add tests for authProviders and singleAuthEnabled when loading from whitelabel.

## Validate

```bash
cd /Users/yona/dev/skybridge/osmt/ui
npm run lint
npm run ci-test
```
