# Split Deployment Design

## Scope of Work

Implement the ability to deploy OSMT as two separate instances:

1. **Public-facing read-only instance**: No write access to the database. Used for public skill browsing and sharing.
2. **Author-facing writable instance**: Full write access for content authors. Visually distinct with different branding.

This is an optional deployment pattern - not all OSMT instances need to use split deployment.

## File Structure

```
api/
├── src/main/resources/config/
│   └── application-readonly.properties   # NEW: Read-only profile meta-config
├── src/main/kotlin/edu/wgu/osmt/
│   ├── config/
│   │   ├── AppConfig.kt                  # UPDATE: Add readOnlyMode flag
│   │   └── WhitelabelConfig.kt           # UPDATE: Add new env vars
│   ├── security/
│   │   └── ReadOnlySecurityConfig.kt     # NEW: No-auth security config
│   └── ui/
│       └── UiController.kt               # UPDATE: Add split deployment fields
└── docker/whitelabel/
    └── whitelabel.json                   # UPDATE: Add instanceType example

ui/
├── src/app/
│   ├── models/
│   │   └── app-config.model.ts           # UPDATE: Add split deployment fields
│   ├── auth/
│   │   ├── login.component.ts            # UPDATE: Handle readonly/author display
│   │   └── login.component.html          # UPDATE: Add readonly/author messages
│   └── navigation/
│       ├── header.component.ts           # UPDATE: Hide login in readonly
│       └── header.component.html         # UPDATE: Author indicator banner
docs/
└── features/
    └── 2026-04-13-split-deployment.md    # NEW: Deployment guide
```

## Conceptual Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Split Deployment                          │
│                                                                  │
│  ┌─────────────────────┐          ┌─────────────────────────┐   │
│  │  READ-ONLY INSTANCE │          │    WRITABLE INSTANCE    │   │
│  │   (public-facing)   │          │    (author-facing)      │   │
│  │                     │          │                         │   │
│  │  Spring Profile:    │          │  Spring Profile:        │   │
│  │    readonly         │          │    oauth2 (or           │   │
│  │                     │          │    single-auth)         │   │
│  │  Meta-config sets:  │          │                         │   │
│  │  • flyway=false     │          │  Standard auth flow     │   │
│  │  • sessions=none    │          │  Full write access      │   │
│  │  • no security      │          │  Author branding        │   │
│  │                     │          │                         │   │
│  │  Whitelabel:        │          │  Whitelabel:            │   │
│  │    instanceType:    │          │    instanceType:        │   │
│  │      read-only      │          │      writable           │   │
│  │                     │          │    colorBrandAccent1:   │   │
│  │  UI: No login,      │          │      (warning color)    │   │
│  │      public browse  │          │                         │   │
│  │                     │          │  UI: Login required,    │   │
│  │                     │          │      author banner      │   │
│  └─────────────────────┘          └─────────────────────────┘   │
│           │                                    │                │
│           └──────────┬───────────────────────────┘                │
│                      │                                          │
│           ┌──────────▼──────────┐                              │
│           │   Shared Database   │                              │
│           │   (MySQL/Postgres)  │                              │
│           └─────────────────────┘                              │
└─────────────────────────────────────────────────────────────────┘
```

## Main Components and How They Interact

### 1. Read-Only Profile (`application-readonly.properties`)

Acts as a **meta-configuration** that sets lower-level flags:

```properties
# Meta-config: readonly profile
# This file sets lower-level flags to enable read-only mode

# Disable migrations (writable instance handles these)
spring.flyway.enabled=false

# Disable sessions (no auth needed)
spring.session.store-type=none

# Mark as read-only for any code that needs to know
app.readOnlyMode=true

# Instance type for whitelabel
app.instanceType=read-only
```

### 2. Read-Only Security Config (`ReadOnlySecurityConfig.kt`)

A new security configuration that:
- Only activates with the `readonly` profile
- Permits all requests without authentication
- Disables all auth filters
- Still includes CORS configuration

### 3. Whitelabel Extensions

New fields added to whitelabel JSON:

```json
{
  "instanceType": "read-only",
  "writableInstanceUrl": "https://author.example.com",
  "writableInstanceName": "Author Portal",
  "colorBrandAccent1": "#1e40af",
  "readOnlyMessage": "This is the public skill browser. To edit content, visit the Author Portal."
}
```

New environment variables:
- `OSMT_INSTANCE_TYPE` - `"read-only"` or `"writable"`
- `OSMT_WRITABLE_INSTANCE_URL` - URL to writable instance
- `OSMT_WRITABLE_INSTANCE_NAME` - Display name for writable instance
- `OSMT_READ_ONLY_MESSAGE` - Custom message for read-only login page
- `OSMT_BRAND_COLOR` - Can be different per instance

### 4. Frontend UI Changes

**Login Page** (`login.component.html`):
- When `instanceType === 'read-only'`: Show read-only message with link to writable instance
- When `instanceType === 'writable'`: Show normal login UI (or author indicator if configured)

**Header** (`header.component.html`):
- When `instanceType === 'read-only'`: No login button shown
- When `instanceType === 'writable'`: Normal login/auth UI, optional author banner

### 5. Instance Type Detection Flow

```
┌────────────────────────────────────────────────────────────────┐
│  Instance Startup                                              │
│                                                                │
│  1. Spring profiles active:                                    │
│     • readonly → Sets app.readOnlyMode=true                    │
│     • oauth2 OR single-auth → Standard auth                    │
│                                                                │
│  2. Whitelabel JSON generated:                                 │
│     • instanceType from app.instanceType                       │
│     • writableInstanceUrl from env var                         │
│                                                                │
│  3. Frontend loads /whitelabel/whitelabel.json                 │
│                                                                │
│  4. UI adapts:                                                 │
│     • read-only → No auth UI, public browsing                  │
│     • writable → Auth required, full features                  │
└────────────────────────────────────────────────────────────────┘
```

## Key Design Decisions

1. **Meta-config approach**: The `readonly` profile is a high-level configuration that sets multiple lower-level flags (flyway, sessions, security). This keeps the code clean without `if (readOnlyMode)` checks everywhere.

2. **Whitelabel for UI differentiation**: The instance type is communicated to the frontend via the existing whitelabel JSON mechanism, keeping backend and frontend loosely coupled.

3. **Writable instance is visually distinct**: The authoring interface uses different branding (color/logo) to make it clear you're on the special interface. The public read-only instance feels like the normal/default experience.

4. **Optional deployment pattern**: Split deployment is not required. Existing single-instance deployments continue to work unchanged.

5. **Shared database**: Both instances connect to the same database. The read-only instance has no write access at the application level (no routes that mutate) and ideally also at the database permission level.

## Security Considerations

- Read-only instance has no authentication (all requests permitted)
- Read-only instance should have database user with only SELECT permissions
- Writable instance has full auth and write access
- Public URLs always point to read-only instance (shareable, safe)
