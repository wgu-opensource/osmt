# Whitelabel & Theming Improvements

## Scope of Work

Remove WGU-specific branding from the open-source OSMT codebase and replace it
with generic OSMT branding. Add support for deployers to customize branding
(colors, logo, app name, footer text) via environment variables passed to the
Docker container. Deliver documentation in `docs/features/` describing how to
use the branding env vars.

## Current State

### Logo

- **Header logo is hardcoded** in `header.component.html` →
  `/assets/images/logo-dark.svg`
- `logo-dark.svg` and `logo-light.svg` are the **WGU owl logo** (SVG paths,
  white-on-transparent and dark-on-transparent respectively)
- `logo-white-label.svg` exists as a generic openclipart placeholder but is
  **not used anywhere**
- No `logoUrl` field in `IAppConfig` — logo is not whitelabel-configurable today

### Colors

- CSS variables defined in `ui/src/pattern-library/css/screen.css` under `:root`
- `--color-brand1: #022a4d` (WGU dark blue) is the primary brand color
- `app.component.ts` already overrides `--color-brand1` from
  `whitelabel.colorBrandAccent1` if present, including accessibility contrast
  check for `--color-a11yOnBrand`
- Dark mode is handled via `@media (prefers-color-scheme: dark)`

### Whitelabel JSON Mechanism

- **API side**: `UiController.kt` serves `/whitelabel/whitelabel.json` by
  merging a static JSON file with dynamic auth config
- Static file lives at `api/docker/whitelabel/whitelabel.json` (generic OSN
  branding)
- UI-side copy at `ui/src/whitelabel/whitelabel.json` has **WGU-specific
  values** (defaultAuthorValue, licensePrimary)
- `IAppConfig` supports: `toolName`, `toolNameLong`, `colorBrandAccent1`,
  `licensePrimary`, `licenseSecondary`, `poweredBy`, `poweredByUrl`,
  `poweredByLabel`, `defaultAuthorValue`
- `WHITELABEL_PATH` env var lets the API serve whitelabel files from an external
  directory

### Favicon

- `index.html` references `/assets/icons/favicon-*.png`, `apple-touch-icon.png`,
  `safari-pinned-tab.svg`
- `site.webmanifest` has empty `name` and `short_name`
- Favicon files appear to be generic/default — no WGU branding in them

### Docker / Runtime Config

- **UI container**: nginx serving static Angular build. Runtime config via
  `/config/config.json` volume mount → `window.__env`
- **API container**: Spring Boot. Env vars for DB, Redis, ES, OAuth.
  `WHITELABEL_PATH` for external whitelabel directory
- `docker-compose.yml` mounts `./ui/src/config/config.json` into the UI
  container
- **No entrypoint script** currently generates config from env vars for the UI
  container

### WGU-Specific References to Remove

1. `ui/src/whitelabel/whitelabel.json` — "Western Governors University" in
   `defaultAuthorValue` and `licensePrimary`
2. `logo-dark.svg`, `logo-light.svg` — WGU owl SVG
3. `api/Dockerfile`, `ui/Dockerfile` — `LABEL Maintainer="WGU / OSN"`
4. `--color-brand1: #022a4d` — WGU dark blue (should pick a neutral OSMT color)
5. `docs/int/openapi-v2.yaml` — WGU example values

## Questions

### Q1: Logo delivery mechanism for Docker deployments

The logo is currently a static SVG baked into the Angular build. For
whitelabeling via Docker, we need deployers to provide their own logo. Options:

**Option A: Base64 data URI in env var**
- Set `OSMT_LOGO_DATA` env var with a base64-encoded SVG or small PNG
- Entrypoint script writes it into a file served by nginx
- Pro: Single env var, no volume mounts needed
- Con: Env vars have size limits (~128KB on Linux, varies by system); SVGs can
  be large; not human-readable in config

**Option B: URL to external logo**
- Set `OSMT_LOGO_URL` env var with a URL to the logo
- Frontend fetches logo from URL at runtime (or entrypoint downloads it)
- Pro: Clean, no size limits
- Con: External dependency at runtime; CORS issues if fetched client-side

**Option C: Volume mount**
- Mount logo file into container at a known path (e.g.,
  `/usr/share/nginx/html/assets/images/logo-custom.svg`)
- Pro: Standard Docker pattern; works with any file size
- Con: Requires volume mount, more complex than a single env var

**Option D: WHITELABEL_PATH directory (extend existing mechanism)**
- Deployer places logo in the same directory as `whitelabel.json`
- Add a `logoUrl` field to `whitelabel.json` pointing to the path within that
  directory
- Pro: Leverages existing `WHITELABEL_PATH` pattern; one mechanism for all
  customization
- Con: Only works for the API-served config; UI container is separate

**Decided**: Add `logoUrl` to `IAppConfig` / whitelabel JSON. The value can be:
1. A relative path (deployer volume-mounts a file into the container)
2. An absolute URL (externally hosted image)
3. A `data:` URI (for small SVGs/PNGs inline)
The default is the built-in generic OSMT text logo. The existing
`WHITELABEL_PATH` directory mechanism already supports serving arbitrary files,
so deployers using that can drop their logo there and reference it.

### Q2: Default generic OSMT branding

We need to create new default logo assets to replace the WGU owl. Options:

- **Text-only logo**: Simple "OSMT" text in the brand font — minimal, clean,
  easy to generate as SVG
- **Icon + text**: A simple geometric icon (e.g., interlocking shapes
  representing skills) plus "OSMT" text
- **Use existing `logo-white-label.svg`**: The "generic company" openclipart
  placeholder — but it looks unprofessional

**Decided**: Create clean text-only SVG logos saying "OSMT" in a neutral
sans-serif font. Two variants: dark text (`logo-dark.svg`) and white text
(`logo-light.svg`). Replace the WGU owl SVGs. Remove `logo-white-label.svg`.

### Q3: Default brand color

Currently `--color-brand1: #022a4d` (WGU dark navy). We need a new neutral
default.

**Decided**: `#1e40af` (Tailwind `blue-800`) — a clean, professional medium
blue. Dark enough for good white-text contrast, neutral, not associated with
any particular company.

### Q4: Which env vars to support?

The existing whitelabel JSON has many fields. For Docker env var theming, we
could either:

**Option A: One env var per field**
- `OSMT_TOOL_NAME`, `OSMT_BRAND_COLOR`, `OSMT_LOGO_URL`, etc.
- Pro: Simple, each field independently configurable
- Con: Many env vars

**Option B: Single JSON env var**
- `OSMT_WHITELABEL='{"toolName":"My Tool",...}'`
- Pro: Single env var, flexible
- Con: JSON in env vars is awkward to manage

**Option C: Env vars for common fields, JSON for advanced**
- Env vars: `OSMT_TOOL_NAME`, `OSMT_BRAND_COLOR`, `OSMT_LOGO_URL`
- Full override: `OSMT_WHITELABEL_JSON` for complete whitelabel.json content
- Pro: Best of both worlds
- Con: Precedence rules needed

**Decided**: Option C — individual env vars for common fields plus JSON override.

Individual env vars:
- `OSMT_TOOL_NAME` (default: "OSMT")
- `OSMT_TOOL_NAME_LONG` (default: "Open Skills Management Tool")
- `OSMT_BRAND_COLOR` (default: `#1e40af`)
- `OSMT_LOGO_URL` (logo URL/path/data-URI)
- `OSMT_LICENSE_PRIMARY` (footer copyright)
- `OSMT_LICENSE_SECONDARY` (footer secondary)

Full override: `OSMT_WHITELABEL_JSON` — complete JSON blob.

Precedence: individual env vars > `OSMT_WHITELABEL_JSON` > static file.

### Q5: Scope of "generic OSMT branding" for the default

What should the default footer, powered-by, and license text say? Currently the
`api/docker/whitelabel/whitelabel.json` (used in Docker) already says "Open
Skills Network" and the `DefaultAppConfig` in the UI also uses OSN branding.
The `ui/src/whitelabel/whitelabel.json` (used in dev) says "Western Governors
University".

**Decided**: Unify all defaults:
- `licensePrimary`: "Copyright © OSMT Contributors"
- `licenseSecondary`: "All rights reserved."
- `poweredBy` / `poweredByUrl` / `poweredByLabel`: empty strings (deployers
  can set these if they want "Powered by" attribution)
- `defaultAuthorValue`: empty string
- Remove all "Western Governors University" references.

### Q6: Architecture — where does the env-var-to-config translation happen?

Currently the UI and API are separate containers. The whitelabel JSON is served
by the API. For env-var-based theming:

**Option A: API-side only**
- All `OSMT_*` env vars are read by the Spring Boot API
- API generates the whitelabel JSON response dynamically
- Pro: Single place for config logic; UI stays dumb
- Con: Logo file serving is awkward from the API

**Option B: UI entrypoint script**
- Add an nginx entrypoint script that reads `OSMT_*` env vars and generates
  `/config/config.json` and/or writes logo files
- Pro: UI container is self-contained for theming
- Con: Duplicates config logic

**Option C: Both — API for whitelabel JSON, UI entrypoint for logo**
- API reads env vars and generates whitelabel JSON with all text/color config
- UI entrypoint handles logo file placement
- Pro: Each container handles what it serves
- Con: More complex

**Decided**: Option A — API-side only. The Spring Boot API reads `OSMT_*` env
vars and merges them into the `/whitelabel/whitelabel.json` response.
Precedence: individual env vars > `OSMT_WHITELABEL_JSON` > static file.

For the logo: `logoUrl` field in whitelabel JSON tells the frontend what
`<img src>` to use. Default points to built-in `/assets/images/logo-dark.svg`.
No UI entrypoint script needed.
