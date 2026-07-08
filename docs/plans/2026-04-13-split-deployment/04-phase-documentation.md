# Phase 4: Documentation and Deployment Guide

## Scope of Phase

Create comprehensive documentation for split deployment:

1. Feature document explaining split deployment concept
2. Configuration reference for all new environment variables
3. Example deployment configurations

## Code Organization Reminders

- Prefer a granular file structure, one concept per file
- Place more abstract things, entry points, and tests **first**
- Place helper utility functions **at the bottom** of files
- Keep related functionality grouped together
- Any temporary code should have a TODO comment so we can find it later

## Implementation Details

### 1. Create Feature Document

**File**: `docs/features/2026-04-13-split-deployment.md`

```markdown
# OSMT Split Deployment

This document describes how to deploy OSMT as two separate instances: a public-facing read-only instance and an author-facing writable instance.

## Overview

Split deployment is an **optional** security pattern for organizations that want to:

1. Provide public access to skills and collections without any write risk
2. Keep the authoring interface separate and clearly identified
3. Ensure all published/shareable URLs point to the read-only instance

### Architecture

```
┌─────────────────────┐     ┌─────────────────────────┐
│  READ-ONLY INSTANCE │     │    WRITABLE INSTANCE    │
│   (public-facing)   │     │    (author-facing)      │
│                     │     │                         │
│  • No authentication│     │  • Full authentication  │
│  • Public browsing  │     │  • Full write access    │
│  • No database      │     │  • Database migrations  │
│    migrations       │     │  • Author branding      │
│  • Standard branding│     │                         │
│                     │     │                         │
└─────────┬───────────┘     └───────────┬─────────────┘
          │                             │
          └──────────┬──────────────────┘
                     │
          ┌──────────▼──────────┐
          │   Shared Database   │
          │   (read-only user   │
          │    for public,      │
          │    write user for   │
          │    author)          │
          └─────────────────────┘
```

## Quick Start

### 1. Configure the Read-Only Instance

Set the Spring profile to `readonly`:

```bash
export SPRING_PROFILES_ACTIVE=readonly
```

Or in Docker:

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=readonly
```

### 2. Configure Environment Variables

#### Read-Only Instance

| Variable | Value | Description |
|----------|-------|-------------|
| `SPRING_PROFILES_ACTIVE` | `readonly` | Activates read-only mode |
| `OSMT_INSTANCE_TYPE` | `read-only` | Frontend label |
| `OSMT_WRITABLE_INSTANCE_URL` | `https://author.example.com` | Link to writable instance |
| `OSMT_WRITABLE_INSTANCE_NAME` | `Author Portal` | Display name for link |
| `OSMT_READ_ONLY_MESSAGE` | (optional) | Custom message on login page |

#### Writable Instance

| Variable | Value | Description |
|----------|-------|-------------|
| `SPRING_PROFILES_ACTIVE` | `oauth2` or `single-auth` | Standard auth profile |
| `OSMT_INSTANCE_TYPE` | `writable` | Frontend label |
| `OSMT_BRAND_COLOR` | `#e65100` | Different color (e.g., orange) |
| `OSMT_LOGO_URL` | `/assets/images/logo-author.svg` | Different logo |

### 3. Database Permissions

The read-only database user should have only SELECT permissions:

```sql
-- Create read-only user
CREATE USER 'osmt_readonly'@'%' IDENTIFIED BY 'password';

-- Grant only SELECT on all tables
GRANT SELECT ON osmt_db.* TO 'osmt_readonly'@'%';

-- Flush privileges
FLUSH PRIVILEGES;
```

The writable instance uses a user with full permissions.

## Configuration Reference

### Spring Profiles

| Profile | Purpose |
|-----------|---------|
| `readonly` | Read-only public instance. No auth, no migrations. |
| `oauth2` | OAuth2 authentication (production) |
| `single-auth` | Username/password authentication (development) |

Profiles can be combined: `oauth2,single-auth` for staging with both options.

### Environment Variables

#### Core Split Deployment

| Variable | Default | Description |
|----------|---------|-------------|
| `OSMT_INSTANCE_TYPE` | `writable` | `read-only` or `writable` |
| `OSMT_WRITABLE_INSTANCE_URL` | (empty) | URL to writable instance |
| `OSMT_WRITABLE_INSTANCE_NAME` | `Author Portal` | Display name for writable instance link |
| `OSMT_READ_ONLY_MESSAGE` | (default) | Message shown on read-only login page |

#### Whitelabel/Theming

| Variable | Default | Description |
|----------|---------|-------------|
| `OSMT_BRAND_COLOR` | `#1e40af` | Primary brand color (different per instance) |
| `OSMT_LOGO_URL` | `/assets/images/logo-light.svg` | Logo URL (different per instance) |
| `OSMT_TOOL_NAME` | `OSMT` | Tool name |
| `OSMT_TOOL_NAME_LONG` | `Open Skills Management Tool` | Full tool name |

## UI Behavior

### Read-Only Instance

- **No login button**: The header menu does not show login/logout
- **Login page shows message**: Explains this is the public browser with link to writable instance
- **Standard branding**: Uses default/standard OSMT branding
- **Public browsing**: Anyone can search and view skills/collections

### Writable Instance

- **Authentication required**: Normal login flow (OAuth2 or single-auth)
- **Full features**: Workspace, sync, skill editing, etc.
- **Author branding**: Different color and/or logo to indicate this is the authoring interface
- **Login page**: Normal login UI (OAuth buttons or username/password form)

## Example: Docker Compose

```yaml
version: '3.8'

services:
  # Read-only public instance
  osmt-public:
    image: osmt:latest
    environment:
      - SPRING_PROFILES_ACTIVE=readonly
      - DB_USER=osmt_readonly
      - DB_PASSWORD=readonly_password
      - OSMT_INSTANCE_TYPE=read-only
      - OSMT_WRITABLE_INSTANCE_URL=https://author.example.com
      - OSMT_WRITABLE_INSTANCE_NAME=Author Portal
    ports:
      - "8080:8080"

  # Writable author instance
  osmt-author:
    image: osmt:latest
    environment:
      - SPRING_PROFILES_ACTIVE=oauth2
      - DB_USER=osmt_write
      - DB_PASSWORD=write_password
      - OAUTH_GOOGLE_CLIENT_ID=xxx
      - OAUTH_GOOGLE_CLIENT_SECRET=xxx
      - OSMT_INSTANCE_TYPE=writable
      - OSMT_BRAND_COLOR=#e65100
      - OSMT_LOGO_URL=/assets/images/logo-author.svg
    ports:
      - "8081:8080"
```

## Security Considerations

1. **Database permissions**: Ensure the read-only instance uses a database user with only SELECT permissions
2. **Network isolation**: Consider placing the writable instance on a private network accessible only to authors
3. **Rate limiting**: Implement rate limiting on the public instance to prevent abuse
4. **HTTPS**: Both instances should use HTTPS in production
5. **URL sharing**: Always share public instance URLs (skills, collections) - they are safe and read-only

## Troubleshooting

### Read-only instance shows login form

Check that `SPRING_PROFILES_ACTIVE=readonly` is set. Without this profile, the instance will behave as a normal writable instance.

### Writable instance branding not applied

Ensure `OSMT_INSTANCE_TYPE=writable` is set and whitelabel JSON is being served correctly:

```bash
curl https://author.example.com/whitelabel/whitelabel.json | jq
```

### Database errors on read-only instance

The read-only instance should have `spring.flyway.enabled=false` via the `readonly` profile. If you see migration errors, the profile isn't active.

## Related Documentation

- [Authentication](2026-02-28-auth.md) - OAuth2 and single-auth configuration
- [Whitelabel & Theming](2026-03-19-whitelabel-theming.md) - Branding customization
```

### 2. Update Whitelabel Theming Document

Add a section to `docs/features/2026-03-19-whitelabel-theming.md` referencing split deployment:

```markdown
## Split Deployment Theming

When using [split deployment](2026-04-13-split-deployment.md) (separate read-only and writable instances), you can use whitelabel to differentiate the two:

### Read-Only Instance (Public)

Use standard, neutral branding:

```bash
OSMT_BRAND_COLOR=#1e40af
OSMT_LOGO_URL=/assets/images/logo-light.svg
OSMT_INSTANCE_TYPE=read-only
```

### Writable Instance (Authors)

Use distinct branding to indicate this is the authoring interface:

```bash
OSMT_BRAND_COLOR=#e65100  # Orange/warning color
OSMT_LOGO_URL=/assets/images/logo-author.svg
OSMT_INSTANCE_TYPE=writable
```

This visual distinction helps authors know they're on the special interface with full write access.
```

## Validate

Preview the documentation:

```bash
# Check markdown formatting
cd /Users/yona/dev/skybridge/osmt/docs/features
npx prettier --write 2026-04-13-split-deployment.md

# Verify links work (manual check)
# - Link to auth documentation
# - Link to whitelabel documentation
```

Ensure the document is discoverable:
- Add to `docs/README.md` table of contents if one exists
- Or ensure it's linked from the main documentation
