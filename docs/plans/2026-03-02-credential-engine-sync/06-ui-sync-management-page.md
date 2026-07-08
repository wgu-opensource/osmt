# Phase 6: UI – Sync Management Page

## Scope of Phase

- /admin/sync route
- SyncManagementComponent: list integrations, watermarks, Sync now button
- Disabled state when CE not configured and mock not used
- Nav link for admin users

## Code Organization Reminders

- One component per file; template and styles separate
- Use existing Angular patterns (AuthGuard, services)
- Follow .eslintrc, .prettierrc, 80-char lines

## Implementation Details

### 1. API Client

Create or extend a service to call sync API:
- `GET /api/sync/state` → SyncStateResponse
- `POST /api/sync/all` → trigger sync

Add to existing `environment` or create `SyncService` in UI. Use `HttpClient` with baseApiUrl.

### 2. SyncManagementComponent

Create `ui/src/app/admin/sync/sync-management.component.ts`:

- Fetch sync state on init
- Display: table or list of integrations (syncKey, recordType, syncWatermark)
- Button "Sync Now" → POST /api/sync/all
- On success: show toast; refresh state
- On 503: show "Sync is not configured" message; disable button
- On 401: redirect to login (handled by guard)

Create `sync-management.component.html`:
- Simple layout: heading, table of integrations, Sync Now button
- Disabled state: gray out button, show message when sync not configured

Create `sync-management.component.spec.ts`: unit tests with mocked HTTP.

### 3. Routing

Update `ui/src/app/app-routing.module.ts`:
```typescript
{
  path: 'admin/sync',
  component: SyncManagementComponent,
  canActivate: [AuthGuard],
  data: { roles: ActionByRoles.get(ButtonAction.SyncManage) },
}
```

Add `ButtonAction.SyncManage` and `[ButtonAction.SyncManage, [OSMT_ADMIN]]` to auth-roles.ts.

### 4. Nav Link

Update `header.component.html` (and `commoncontrols-mobile.component.html` if applicable):
- Add "Sync" or "Admin / Sync" link, visible only when user has admin role
- Use `*ngIf` with authService.hasRole or similar
- Link: `/admin/sync`

### 5. Sync Not Configured

When GET /api/sync/state returns 503, or when response indicates disabled: show message "Credential Engine sync is not configured. Set CREDENTIAL_ENGINE_* environment variables to enable."

## Tests

- sync-management.component.spec.ts: render; mock state fetch; click Sync Now; verify 503 handling

## Validate

```bash
cd ui && npm run format:check
cd ui && npm run test
cd ui && npm run build
```
