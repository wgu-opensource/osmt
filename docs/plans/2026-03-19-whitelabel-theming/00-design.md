# Whitelabel & Theming — Design

## Scope of Work

Remove WGU-specific branding from the open-source OSMT codebase and replace it
with generic OSMT branding. Add support for deployers to customize branding
(colors, logo, app name, footer text) via environment variables passed to the
Docker container. Deliver documentation in `docs/features/` describing how to
use the branding env vars.

## File Structure

```
api/
└── src/main/kotlin/edu/wgu/osmt/
    ├── config/
    │   └── AppConfig.kt                     # UPDATE: add OSMT_* env var reading
    └── ui/
        └── UiController.kt                  # UPDATE: merge env vars into whitelabel response

api/docker/whitelabel/
└── whitelabel.json                          # UPDATE: generic OSMT defaults

ui/src/
├── app/
│   ├── models/
│   │   └── app-config.model.ts              # UPDATE: add logoUrl field
│   ├── navigation/
│   │   └── header.component.html            # UPDATE: use whitelabel.logoUrl
│   │   └── header.component.ts              # UPDATE: (minor if needed)
│   └── app.component.ts                     # UPDATE: (minor if needed)
├── pattern-library/
│   ├── images/
│   │   ├── logo-dark.svg                    # REPLACE: generic "OSMT" text logo (dark)
│   │   ├── logo-light.svg                   # REPLACE: generic "OSMT" text logo (light)
│   │   └── logo-white-label.svg             # DELETE: unused placeholder
│   └── css/
│       └── screen.css                       # UPDATE: --color-brand1 to #1e40af
├── whitelabel/
│   └── whitelabel.json                      # UPDATE: generic OSMT defaults
└── index.html                               # UPDATE: site.webmanifest name if needed

api/Dockerfile                               # UPDATE: remove WGU maintainer label
ui/Dockerfile                                # UPDATE: remove WGU maintainer label

docs/features/
└── 2026-03-19-whitelabel-theming.md         # NEW: deployment branding documentation
```

## Conceptual Architecture

```
┌─────────────────────────────────────────────────┐
│                  Deployer                        │
│  Sets env vars: OSMT_TOOL_NAME, OSMT_BRAND_COLOR│
│  OSMT_LOGO_URL, OSMT_LICENSE_*, etc.            │
│  Optional: OSMT_WHITELABEL_JSON (full override) │
└──────────────────────┬──────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────┐
│              API Container (Spring Boot)          │
│                                                   │
│  GET /whitelabel/whitelabel.json                  │
│  ┌─────────────────────────────────────────────┐  │
│  │ 1. Load static whitelabel.json (defaults)   │  │
│  │ 2. Overlay OSMT_WHITELABEL_JSON if set      │  │
│  │ 3. Overlay individual OSMT_* env vars       │  │
│  │ 4. Merge dynamic auth config (as today)     │  │
│  │ 5. Return merged JSON                       │  │
│  └─────────────────────────────────────────────┘  │
│                                                   │
│  Optional: WHITELABEL_PATH serves logo files      │
└──────────────────────┬───────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────┐
│              UI Container (Angular/nginx)          │
│                                                   │
│  AppConfig.load() fetches /whitelabel/whitelabel  │
│  ┌─────────────────────────────────────────────┐  │
│  │ app.component.ts:                           │  │
│  │   - Sets --color-brand1 from brandColor     │  │
│  │   - Sets page title from toolName           │  │
│  │                                             │  │
│  │ header.component.html:                      │  │
│  │   - <img [src]="whitelabel.logoUrl">        │  │
│  │                                             │  │
│  │ footer.component.html:                      │  │
│  │   - {{ whitelabel.licensePrimary }}         │  │
│  │   - {{ whitelabel.poweredBy... }}           │  │
│  └─────────────────────────────────────────────┘  │
│                                                   │
│  Built-in default: /assets/images/logo-dark.svg   │
│  (generic "OSMT" text logo)                       │
└──────────────────────────────────────────────────┘
```

## Main Components

### Env Var Merging (API)

The `UiController.whitelabelConfig()` method is extended to read `OSMT_*` env
vars and merge them into the response. Merge order (later wins):

1. Static `whitelabel.json` from classpath (or `WHITELABEL_PATH`)
2. `OSMT_WHITELABEL_JSON` env var (parsed as JSON, overlays all fields)
3. Individual `OSMT_*` env vars (overlay specific fields)
4. Dynamic auth config (loginUrl, authMode, authProviders, singleAuthEnabled)

### Supported Env Vars

| Env Var | Whitelabel Field | Default |
|---------|-----------------|---------|
| `OSMT_TOOL_NAME` | `toolName` | `"OSMT"` |
| `OSMT_TOOL_NAME_LONG` | `toolNameLong` | `"Open Skills Management Tool"` |
| `OSMT_BRAND_COLOR` | `colorBrandAccent1` | `"#1e40af"` |
| `OSMT_LOGO_URL` | `logoUrl` | `"/assets/images/logo-dark.svg"` |
| `OSMT_LICENSE_PRIMARY` | `licensePrimary` | `"Copyright © OSMT Contributors"` |
| `OSMT_LICENSE_SECONDARY` | `licenseSecondary` | `"All rights reserved."` |
| `OSMT_WHITELABEL_JSON` | *(all fields)* | *(none)* |

### Logo Handling (UI)

`logoUrl` is a new field on `IAppConfig`. The header template binds to it:
`<img [src]="whitelabel.logoUrl">`. The value can be:

- A relative path: `/assets/images/logo-dark.svg` (default, built-in)
- An absolute URL: `https://example.com/my-logo.svg`
- A data URI: `data:image/svg+xml;base64,...`

### Default Branding

- Logo: text-only "OSMT" SVG in neutral sans-serif
- Brand color: `#1e40af` (medium blue)
- Footer: "Copyright © OSMT Contributors / All rights reserved."
- Powered-by: empty by default
- Author: empty by default
