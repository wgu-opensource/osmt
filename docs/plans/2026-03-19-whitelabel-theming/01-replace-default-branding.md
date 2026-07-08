# Phase 1: Replace Default Branding Assets & Text

## Scope

Remove all WGU-specific branding from the codebase and replace with generic
OSMT defaults. This phase touches only static assets and default config values —
no new features yet.

## Code Organization Reminders

- Prefer a granular file structure, one concept per file.
- Place more abstract things, entry points, and tests **first**
- Place helper utility functions **at the bottom** of files.
- Keep related functionality grouped together
- Any temporary code should have a TODO comment so we can find it later.

## Implementation Details

### 1. Replace logo SVGs

Replace `ui/src/pattern-library/images/logo-dark.svg` with a clean text-only
SVG saying "OSMT" in a neutral sans-serif font, dark fill (`#1e40af` or
`#1f2937`) on transparent background. Approximate size: `viewBox="0 0 110 24"`
to match the existing `width="110" height="24"` in the header template.

Replace `ui/src/pattern-library/images/logo-light.svg` with the same but white
fill (`#ffffff`) on transparent background.

Delete `ui/src/pattern-library/images/logo-white-label.svg` (unused).

### 2. Update default brand color

In `ui/src/pattern-library/css/screen.css`, change:

```css
--color-brand1: #022a4d;
```

to:

```css
--color-brand1: #1e40af;
```

### 3. Unify whitelabel JSON defaults

**`ui/src/whitelabel/whitelabel.json`** — update to:

```json
{
  "editableAuthor": true,
  "defaultAuthorValue": "",
  "toolName": "OSMT",
  "toolNameLong": "Open Skills Management Tool",
  "publicSkillTitle": "Rich Skill Descriptor",
  "publicCollectionTitle": "Rich Skill Descriptor Collection",
  "licensePrimary": "Copyright © OSMT Contributors",
  "licenseSecondary": "All rights reserved.",
  "poweredBy": "",
  "poweredByUrl": "",
  "poweredByLabel": ""
}
```

**`api/docker/whitelabel/whitelabel.json`** — update to match the same values.

### 4. Update DefaultAppConfig

In `ui/src/app/models/app-config.model.ts`, update `DefaultAppConfig`:

```typescript
licensePrimary = 'Copyright © OSMT Contributors';
licenseSecondary = 'All rights reserved.';
poweredBy = '';
poweredByUrl = '';
poweredByLabel = '';
```

### 5. Update Dockerfile labels

In both `api/Dockerfile` and `ui/Dockerfile`, change:

```dockerfile
LABEL Maintainer="WGU / OSN"
```

to:

```dockerfile
LABEL Maintainer="OSMT Contributors"
```

### 6. Update site.webmanifest

In `ui/src/pattern-library/icons/site.webmanifest`, set:

```json
"name": "OSMT",
"short_name": "OSMT"
```

### 7. Clean up OpenAPI example values

In `docs/int/openapi-v2.yaml`, replace "Western Governors University" and
"WGUSID" example values with generic ones.

## Tests

- Run existing UI tests to confirm nothing breaks: `npm run ci-test`
  (from `ui/`)
- Run existing API tests: `sdk env && ./mvnw test` (from `api/`)
- Visually confirm the dev server shows the new OSMT text logo and blue brand
  color

## Validate

```bash
cd ui && npm run ci-test && npm run format:check
cd ../api && sdk env && ./mvnw test
```
