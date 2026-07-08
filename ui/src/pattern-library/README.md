# Vendored WGU pattern library (static assets)

This tree is a copy of **`@concentricsky/wgu-design-system-patternlibrary`
version 13.0.0** `dist/` output, committed in-repo so the UI does not depend
on that npm package.

Upstream SCSS / Fractal sources are not available; only built CSS and static
files are vendored. Prefer **Tailwind** and app SCSS for new UI (see
`docs/features/2026-03-04-tailwind-css.md`).

Do not upgrade casually: diff against a newer package `dist/` if you ever
replace this snapshot.
