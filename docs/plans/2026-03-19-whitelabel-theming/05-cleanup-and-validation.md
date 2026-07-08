# Phase 5: Cleanup & Validation

## Scope

Final cleanup, remove any remaining WGU references, validate all tests pass,
and commit.

## Cleanup & Validation

### 1. Grep for remaining WGU references

Search the git diff and the full codebase for any remaining references:

```bash
git diff main --name-only | xargs rg -i "wgu|western governors" || true
rg -i "western governors" --type-not=md ui/ api/
rg "WGU" --type-not=md ui/ api/ | grep -v "edu.wgu.osmt"
```

The `edu.wgu.osmt` Java package name is structural and does not need to change
in this scope — it's not user-facing. But any user-visible "WGU" or "Western
Governors" strings must be removed.

### 2. Grep for temporary code

```bash
git diff main | rg "TODO|FIXME|HACK|XXX|TEMP"
```

Remove any temporary markers added during implementation.

### 3. Run all tests

```bash
cd ui && npm run ci-test && npm run format:check
cd ../api && sdk env && ./mvnw test
```

Fix all warnings, errors, and formatting issues.

### 4. Visual smoke test

Start the dev server and verify:
- New OSMT text logo appears in header
- Brand color is the new blue (`#1e40af`)
- Footer shows "Copyright © OSMT Contributors"
- No "Western Governors University" text anywhere
- Powered-by section is hidden (since defaults are empty)

## Plan Cleanup

1. Add a summary of the completed work to
   `docs/plans/2026-03-19-whitelabel-theming/summary.md`
2. Move the plan directory to `docs/plans-done/`

## Commit

Commit with message following Conventional Commits format:

```
feat(ui,api): replace WGU branding with generic OSMT theming

- Replace WGU owl logos with generic "OSMT" text SVGs
- Update default brand color to #1e40af
- Unify whitelabel.json defaults across UI and API
- Add logoUrl field to IAppConfig for configurable logo
- Add OSMT_* env var support for Docker-based theming
- Hide empty poweredBy section in footer
- Add whitelabel theming documentation
```
