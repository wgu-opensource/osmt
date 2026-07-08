# Pattern library CSS vendored in-repo

The OSMT UI no longer depends on the npm package
`@concentricsky/wgu-design-system-patternlibrary`. Its published **`dist/`**
snapshot (**version 13.0.0**) lives under **`ui/src/pattern-library/`** (CSS,
fonts, icons, images) with a short **`README.md`** there describing provenance.

## Why

The external package was tied to a defunct vendor workflow. Vendoring satisfies
the requirement to maintain styling without a remote artifact, while the
**Tailwind** migration continues separately.

## Build wiring

- **`ui/angular.json`**: global styles load
  **`src/pattern-library/css/screen.css`**, then **`src/styles.scss`**.
- Static files under **`src/pattern-library/`** are copied to build output
  **`assets/`** (same URLs as before: `/assets/images/...`, `/assets/icons/...`).
- App-only assets remain in **`ui/src/assets/`** (e.g. `svg-extra-defs.svg`).

## CSS maintenance

`screen.css` is **Prettier-formatted** for readability. Upstream SCSS was not
preserved; large-scale restructuring is intentionally out of scope until
Tailwind replaces legacy classes.

## Related

- **`docs/features/2026-03-04-tailwind-css.md`** – Tailwind tokens, load order,
  and incremental migration away from pattern-library classes.
