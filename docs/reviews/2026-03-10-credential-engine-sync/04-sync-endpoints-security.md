# Sync Endpoints Not Explicitly Secured in Spring Security

**Severity:** Medium
**File:** `SecurityConfigHelper.kt`, `SyncController.kt`

## Problem

The sync endpoints (`/api/sync/state`, `/api/sync/all`, `/api/sync/resync`, etc.) are not listed in `SecurityConfigHelper`. Their security depends on which mode is active:

**Roles enabled (`enableRoles = true`):**
Falls through to the catch-all:
```kotlin
.requestMatchers("/api/**")
.hasAnyAuthority(admin, curator, view, read)
```
This means any authenticated user with *any* role (including View or Read) can trigger a full resync. The `ensureAdmin()` check in `SyncController` provides the actual admin-only guard.

**No-roles mode (`enableRoles = false`):**
Falls through to:
```kotlin
.requestMatchers("/**")
.permitAll()
```
The sync endpoints are **publicly accessible** at the Spring Security level. Only the application-level `ensureAdmin()` check prevents unauthorized access. If there's a bug in `OAuthHelper.hasRole()` or the auth context is misconfigured, the sync endpoints are wide open.

## Impact

- Defense-in-depth violation: a single bug in `ensureAdmin()` or `OAuthHelper` exposes sync operations.
- In roles-enabled mode, View/Read users can hit sync endpoints (blocked by `ensureAdmin()` but permitted through the door by Spring Security).
- Sync operations are destructive (resync clears watermarks and re-publishes everything to CE).

## Fix

Add explicit Spring Security rules for sync endpoints in `SecurityConfigHelper`:

```kotlin
// In configureRoleBasedEndpoints:
.requestMatchers(GET, "/api/sync/**")
.hasAnyAuthority(admin)
.requestMatchers(POST, "/api/sync/**")
.hasAnyAuthority(admin)

// In configureNoRoleEndpoints:
.requestMatchers("/api/sync/**")
.authenticated()
```

This makes Spring Security the first line of defense and `ensureAdmin()` the second. The controller check is still valuable as a guard against misconfiguration.
