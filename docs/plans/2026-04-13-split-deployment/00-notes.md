# Split Deployment: Read-Only Public Instance + Writable Author Instance

## Scope of Work

Implement the ability to deploy OSMT as two separate instances:

1. **Public-facing read-only instance**: No write access to the database or any other resources. Used for public skill browsing and sharing.
2. **Author-facing writable instance**: Full write access for content authors. Used for creating, editing, and managing skills and collections.

This is a client security requirement. All published/shareable URLs must point to the public read-only instance.

## Three Major Parts

1. **Changes to OSMT itself** (this plan): Backend and frontend modifications to support read-only mode and writable instance URL configuration.
2. **Changes to example deployment** (out of scope for this plan, kept in separate monorepo): Infrastructure and deployment configuration changes.
3. **Instructional document** (part of this plan): Documentation covering how to deploy OSMT in split mode.

This plan focuses on parts 1 and 3.

## Changes Required to OSMT

### 1. Read-Only Mode Flag

A configuration flag to indicate the instance is running in read-only mode:

- Backend property: `app.readOnlyMode` (default: `false`)
- Environment variable: `OSMT_READ_ONLY_MODE`
- When `true`:
  - All mutating API endpoints (POST, PUT, DELETE, PATCH) return 403 Forbidden
  - Database migrations are disabled (Flyway should not run)
  - Login is disabled (no authentication required/allowed)
  - Redis session management can be disabled or minimized

### 2. Writable Instance URL Configuration

A configuration to indicate where the writable instance lives, for generating correct URLs in public-facing content:

- Backend property: `app.writableInstanceUrl`
- Environment variable: `OSMT_WRITABLE_INSTANCE_URL`
- Frontend: exposed via whitelabel JSON as `writableInstanceUrl`
- Used for:
  - "Edit this skill" links on public skill pages (point to writable instance)
  - Collection management links
  - Any "take action" URLs in shared/public content

### 3. Frontend UI Adjustments

When in read-only mode:

- **No login button in menu**: The header menu should not show login/logout buttons
- **Read-only indicator**: A visual indicator (theme color change + banner/message) that this is the read-only public instance
- **Login page message**: If someone navigates to `/login` directly, show a message explaining this is the read-only instance with a link to the writable instance
- **Customizable message**: Environment-configurable message explaining the instance is read-only (e.g., "This is the public skill browser. To edit content, visit the [author portal]")

### 4. Whitelabel Enhancements

New whitelabel fields for split deployment:

- `instanceType`: `"read-only"` or `"writable"` (or `isReadOnly: boolean`)
- `writableInstanceUrl`: URL to the writable instance
- `readOnlyMessage`: Customizable message shown on login page for read-only instances
- `readOnlyBannerText`: Optional banner text for read-only instances (empty = no banner)

New environment variables:

- `OSMT_INSTANCE_TYPE`: `"read-only"` or `"writable"`
- `OSMT_WRITABLE_INSTANCE_URL`: URL to writable instance
- `OSMT_READ_ONLY_MESSAGE`: Message explaining read-only status
- `OSMT_READ_ONLY_BANNER_TEXT`: Banner text (optional)
- `OSMT_BRAND_COLOR_READONLY`: Optional different brand color for read-only instance (falls back to `OSMT_BRAND_COLOR`)

## Current State of Codebase

### Whitelabel System

- **Backend**: `UiController.kt` serves `/whitelabel/whitelabel.json` by merging static JSON with dynamic auth config
- **Environment variable support**: `WhitelabelConfig.kt` reads `OSMT_*` env vars and merges them
- **Frontend**: `AppConfig` loads whitelabel JSON and applies it to `IAppConfig`

### Security Configuration

- **OAuth2 profile**: `SecurityConfig.kt` handles OAuth2 + optional single-auth
- **Single-auth profile**: `SingleAuthSecurityConfig.kt` for local dev
- **Public endpoints**: Configured via `SecurityConfigHelper.kt`
- **Roles**: Admin, Curator, View roles with configurable endpoints

### Authentication Flow

- **Login page**: `login.component.ts` shows OAuth providers and/or single-auth form
- **Header**: `header.component.ts` shows login/logout based on auth state
- **Auth service**: `auth-service.ts` manages tokens and auth state

### Existing Read-Only-ish Features

- `app.allowPublicSearching`: Allow unauthenticated skill search
- `app.allowPublicLists`: Allow unauthenticated collection viewing
- Public skill/collection endpoints exist for canonical URLs

## Questions

### Q1: How should mutating endpoints behave in read-only mode?

Options:

**Option A: 403 Forbidden with custom error message**
- Return 403 with body explaining this is a read-only instance
- Include writable instance URL in error response

**Option B: 301/302 Redirect to writable instance**
- POST/PUT/DELETE requests redirect to equivalent endpoint on writable instance
- Could be confusing for API clients

**Option C: 501 Not Implemented**
- Semantically correct but less informative than 403

**Answer**: Option A - 403 Forbidden with informative error message and writable instance URL.

### Q2: How should database migrations work in split deployment?

In a split deployment:
- The writable instance is the only one that should run Flyway migrations
- The read-only instance should never attempt migrations (it has no write access anyway)

Options:

**Option A: `spring.flyway.enabled=false` in read-only mode**
- Simple, explicit
- Read-only instance never touches Flyway

**Option B: Flyway runs in "validate" mode only**
- Validates schema but doesn't migrate
- Could fail if writable instance hasn't migrated yet

**Answer**: Option A - completely disable Flyway in read-only mode.

### Q3: What should the login page show in read-only mode?

Options:

**Option A: Login form still works (but pointless)**
- User can log in but can't do anything
- Confusing

**Option B: Hide all login UI, show read-only message**
- No OAuth buttons, no single-auth form
- Show the customizable read-only message
- Link to writable instance login

**Option C: Show disabled login UI with message**
- Show login options but disabled/greyed out
- Explain why they're disabled

**Answer**: Option B - completely hide login UI, show clear message with link to writable instance.

### Q4: How should the header menu behave in read-only mode?

Options:

**Option A: Show login button that redirects to writable instance login**
- Login button goes to writable instance
- User authenticates there, then what?

**Option B: No login button at all**
- Clean, simple
- User can't accidentally try to authenticate on read-only instance

**Option C: "Author Portal" link instead of login**
- Replaces login button with link to writable instance
- Clear call-to-action for authors

**Answer**: Option B - no login button at all in read-only mode.

### Q5: Should the read-only instance have Redis/session management at all?

Options:

**Option A: Keep Redis for potential future features**
- Sessions could be used for tracking, rate limiting, etc.
- More complex

**Option B: Disable session management entirely in read-only mode**
- Simpler deployment
- No session state to manage

**Answer**: Option B - completely disable session/auth infrastructure in read-only mode for simplicity and security.

### Q6: How should public skill/collection URLs work between instances?

Both instances share the same database (read-only has read-only access). Public canonical URLs should work on both:

- `https://public-osmt.example.com/skills/{uuid}` - read-only instance
- `https://author-osmt.example.com/skills/{uuid}` - writable instance (same skill)

**Note**: In read-only mode, there is no authentication and thus no "Edit" buttons or workspace features. The read-only instance is purely for public browsing of skills and collections.

**Answer**: No edit links needed in read-only mode since there's no authentication.

### Q7: Where should the "authoring instance" indicator appear?

**Context**: The public read-only instance should feel like the normal/default experience. The writable (authoring) instance should clearly indicate it's the special interface for content authors.

Options:

**Option A: Banner on writable instance login page**
- Login page shows "Authoring Interface" message
- Maybe dismissible banner on all pages when authenticated

**Option B: Theme color/logo difference on writable instance**
- Different brand color (e.g., orange/warning color) for writable instance
- Different logo variant
- No text banner needed

**Option C: Both banner and visual differentiation**
- Visual theme difference (color/logo)
- Banner on login page explaining this is the authoring interface

**Answer**: Different primary color + logo for visual differentiation, plus
customizable login page copy on the authoring instance. The public read-only
instance has no special banner. Split deployment remains optional.

### Q8: Should we create a new Spring profile for read-only mode?

Options:

**Option A: New profile `readonly`**
- `application-readonly.properties` with read-only defaults
- Can combine with other profiles: `oauth2,readonly`

**Option B: Feature flag in existing profiles**
- `app.readOnlyMode=true` in any profile
- Simpler but less explicit

**Answer**: Option A - `readonly` profile that acts as a meta-config controlling lower-level flags:
- `spring.flyway.enabled=false`
- `spring.session.store-type=none`
- Security config allows all requests (no auth required)
- `app.readOnlyMode=true` (for any code that needs to know)

## Notes

- The existing `allowPublicSearching` and `allowPublicLists` settings are related but different - they control public access to search/collection features, not whether the instance itself is read-only
- Need to ensure the read-only instance can still serve static assets and whitelabel JSON
- Consider rate limiting on the read-only instance (even without auth)
