# Phase 4: Documentation

## Scope

Create `docs/features/2026-03-19-whitelabel-theming.md` documenting how
deployers can customize OSMT branding via environment variables.

## Code Organization Reminders

- Prefer a granular file structure, one concept per file.
- Place more abstract things, entry points, and tests **first**
- Place helper utility functions **at the bottom** of files.
- Keep related functionality grouped together
- Any temporary code should have a TODO comment so we can find it later.

## Implementation Details

### Create `docs/features/2026-03-19-whitelabel-theming.md`

The document should include:

#### 1. Overview

Brief explanation: OSMT supports custom branding via environment variables
passed to the API Docker container.

#### 2. Environment Variables Reference

Table of all supported env vars:

| Env Var | Field | Default | Description |
|---------|-------|---------|-------------|
| `OSMT_TOOL_NAME` | `toolName` | `OSMT` | Short app name (tab title, header) |
| `OSMT_TOOL_NAME_LONG` | `toolNameLong` | `Open Skills Management Tool` | Full name (header tagline) |
| `OSMT_BRAND_COLOR` | `colorBrandAccent1` | `#1e40af` | Primary brand color (hex) |
| `OSMT_LOGO_URL` | `logoUrl` | `/assets/images/logo-dark.svg` | Logo image URL |
| `OSMT_LICENSE_PRIMARY` | `licensePrimary` | `Copyright © OSMT Contributors` | Footer copyright |
| `OSMT_LICENSE_SECONDARY` | `licenseSecondary` | `All rights reserved.` | Footer secondary text |
| `OSMT_WHITELABEL_JSON` | *(all)* | *(none)* | Full JSON override |

#### 3. Logo Options

Document the three ways to provide a custom logo:

**Option A: Volume mount**

```yaml
services:
  api:
    volumes:
      - ./my-logo.svg:/opt/osmt/whitelabel/my-logo.svg
    environment:
      WHITELABEL_PATH: /opt/osmt/whitelabel
      OSMT_LOGO_URL: /whitelabel/my-logo.svg
```

**Option B: External URL**

```yaml
services:
  api:
    environment:
      OSMT_LOGO_URL: "https://example.com/my-logo.svg"
```

**Option C: Data URI**

```yaml
services:
  api:
    environment:
      OSMT_LOGO_URL: "data:image/svg+xml;base64,PHN2Zy..."
```

#### 4. Full JSON Override

Example of using `OSMT_WHITELABEL_JSON` for complete control:

```yaml
services:
  api:
    environment:
      OSMT_WHITELABEL_JSON: >
        {
          "toolName": "My Skills Tool",
          "toolNameLong": "My Organization Skills Tool",
          "colorBrandAccent1": "#dc2626",
          "logoUrl": "https://example.com/logo.svg",
          "licensePrimary": "© 2026 My Organization",
          "licenseSecondary": "All rights reserved.",
          "poweredBy": "Powered by",
          "poweredByUrl": "https://github.com/example/osmt",
          "poweredByLabel": "OSMT"
        }
```

#### 5. Merge Precedence

Explain the merge order:
1. Built-in defaults (static `whitelabel.json`)
2. `OSMT_WHITELABEL_JSON` (overlays all fields)
3. Individual `OSMT_*` env vars (highest precedence)

#### 6. Brand Color Accessibility

Note that OSMT automatically checks contrast between the brand color and white
text. If the contrast ratio is below 4.5:1, it switches to black text on the
brand color for accessibility.

#### 7. Quick Start Example

Minimal docker-compose snippet showing how to customize just the name and color:

```yaml
services:
  api:
    image: osmt-api:latest
    environment:
      OSMT_TOOL_NAME: "SkillsHub"
      OSMT_TOOL_NAME_LONG: "SkillsHub Skills Management"
      OSMT_BRAND_COLOR: "#059669"
      OSMT_LICENSE_PRIMARY: "© 2026 SkillsHub Inc."
```

## Validate

Review the document for accuracy against the implementation from phases 1–3.
Ensure all env var names and defaults match the code.
