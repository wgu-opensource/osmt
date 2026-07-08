# Fix viewer role blank screen due to missing public home/search page

Prevent blank screen for viewer role users by ensuring a public home/search
page exists. Users should not be redirected to authenticate without the
ability to browse public collections.

[ClickUp: 868h23198](https://app.clickup.com/t/868h23198)

## Analysis

**Status: Fixed** (addressed by public pages; see
[`2026-03-18-public-pages.md`](../features/2026-03-18-public-pages.md))

The failure mode was landing on OSMT with no usable read-only surface: the
app redirected toward login or guarded routes while the main library/search
experience expected authentication, producing an empty or unusable first
screen for read-only personas (including unauthenticated visitors).

## Resolution

- **Default route** `/` redirects to `/skills` with **no** `AuthGuard`.
- **`/skills`**, **`/skills/search`**, **`/collections`**, **`/categories`**
  (and matching detail routes) are reachable without login when
  `app.allowPublicLists` / `app.allowPublicSearching` are enabled (defaults
  `true`).
- **`RichSkillsLibraryComponent`** sets `isPublicView` and published/archived
  filters for unauthenticated users; search/filter APIs allow public access
  under the same flags.
- Navbar exposes Skills, Collections, and Categories to everyone.

Together this provides the public home and search/browse flow the bug called
for. Residual edge cases (e.g. OAuth timing, `allowPublic*` disabled) are
configuration or follow-up, not the original “no public landing” gap.
