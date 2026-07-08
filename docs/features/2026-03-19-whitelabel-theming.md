# White-label theming

OSMT supports white-label theming. Out of the box it ships with a generic OSMT
logo, a neutral blue brand color, and default footer text. Every visible
branding element — app name, logo, brand color, and footer copy — can be
overridden for your organization.

## What can be themed

| Element | Where it appears | Default |
|---------|-----------------|---------|
| App name | Browser tab, header tagline | OSMT / Open Skills Management Tool |
| Brand color | Navigation bar, buttons, links | `#1e40af` (medium blue) |
| Logo | Top-left of the navigation bar | Generic white "OSMT" mark (on brand bar) |
| Footer copyright | Bottom of every page | Copyright © OSMT Contributors |
| Footer secondary | Below the copyright line | All rights reserved. |
| "Powered by" | Footer, optional | *(hidden by default)* |

## Quick start

Set environment variables on the **API** container. The UI reads its theme from
the API at runtime — no rebuild required.

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

Only set the variables you want to change; everything else keeps its default.

## Environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `OSMT_TOOL_NAME` | `OSMT` | Short app name (tab title, header) |
| `OSMT_TOOL_NAME_LONG` | `Open Skills Management Tool` | Full name shown as tagline |
| `OSMT_BRAND_COLOR` | `#1e40af` | Primary brand color (hex) |
| `OSMT_LOGO_URL` | `/assets/images/logo-light.svg` | Logo image (see below) |
| `OSMT_LICENSE_PRIMARY` | `Copyright © OSMT Contributors` | Footer line 1 |
| `OSMT_LICENSE_SECONDARY` | `All rights reserved.` | Footer line 2 |
| `OSMT_WHITELABEL_JSON` | *(none)* | Full JSON override (advanced) |

These are set on the API container only. The UI container does not need them.

## Built-in logo files (`logo-light` vs `logo-dark`)

There is **no automatic switching** between files. The header uses one URL:
`logoUrl` from whitelabel config (or your `OSMT_LOGO_URL` override).

The top navigation bar uses your **brand color** as its background. The
default logo must be a **light-colored** mark on that bar:

| File | Mark color | Typical use |
|------|------------|-------------|
| `logo-light.svg` | White / light fill | **Default** — visible on the brand-colored navbar |
| `logo-dark.svg` | Brand blue (`#1e40af`) | Light backgrounds (e.g. if you reuse the asset in email or docs) |

If you replace the logo with your own art, use a version that contrasts with
your `OSMT_BRAND_COLOR` wherever the header shows it.

## Providing a custom logo

`OSMT_LOGO_URL` accepts three kinds of value:

### External URL

Point to a logo hosted on your CDN or asset server.

```yaml
environment:
  OSMT_LOGO_URL: "https://cdn.example.com/logo.svg"
```

### Volume mount

Mount a file into the API container and reference its served path. The
`WHITELABEL_PATH` directory makes files available under `/whitelabel/`.

```yaml
services:
  api:
    volumes:
      - ./branding/my-logo.svg:/opt/osmt/whitelabel/my-logo.svg
    environment:
      WHITELABEL_PATH: /opt/osmt/whitelabel
      OSMT_LOGO_URL: /whitelabel/my-logo.svg
```

### Data URI

For small SVGs you can inline the image. Watch platform-specific env var size
limits.

```yaml
environment:
  OSMT_LOGO_URL: "data:image/svg+xml;base64,PHN2Zy..."
```

The logo is sized about 110 × 28 px in the navigation bar. SVG is recommended.

## Full JSON override (advanced)

For complete control — including optional fields like `poweredBy` — pass a
full JSON object as `OSMT_WHITELABEL_JSON`:

```yaml
environment:
  OSMT_WHITELABEL_JSON: >
    {
      "toolName": "SkillsHub",
      "toolNameLong": "SkillsHub Skills Management",
      "colorBrandAccent1": "#059669",
      "logoUrl": "https://example.com/logo.svg",
      "licensePrimary": "© 2026 Example Org",
      "licenseSecondary": "All rights reserved.",
      "poweredBy": "Powered by",
      "poweredByUrl": "https://example.com",
      "poweredByLabel": "OSMT"
    }
```

Individual `OSMT_*` variables override keys in `OSMT_WHITELABEL_JSON` when
both are set.

## How it works

The API serves branding at `/whitelabel/whitelabel.json`. The Angular UI
fetches this on startup (when `dynamicWhitelabel` is `true`, the default in
production) and applies the values to the page.

Configuration is merged in layers — later layers win:

1. **Built-in defaults** — `docker/whitelabel/whitelabel.json` packaged in the
   API JAR
2. **`OSMT_WHITELABEL_JSON`** — full JSON overlay from the env var
3. **Individual `OSMT_*` env vars** — override specific fields
4. **Auth fields** — `loginUrl`, `authMode`, `authProviders`,
   `singleAuthEnabled` from Spring configuration (not theme-related, but
   included in the same response)

### Brand color accessibility

The UI checks the contrast ratio between white text and your brand color. If
the ratio is below 4.5 : 1 (WCAG AA), it automatically switches to dark text
on brand-colored surfaces.

### Relevant source files

| File | Role |
|------|------|
| `api/.../config/WhitelabelConfig.kt` | Reads `OSMT_*` env vars |
| `api/.../config/WhitelabelMerge.kt` | Layer-merge logic |
| `api/.../ui/UiController.kt` | Serves `/whitelabel/whitelabel.json` |
| `ui/src/app/models/app-config.model.ts` | `IAppConfig` interface / defaults |
| `ui/src/app/app.component.ts` | Applies brand color + page title |
| `ui/src/app/navigation/header.component.html` | Logo binding |
| `ui/src/app/navigation/footer.component.html` | Footer text + powered-by |
