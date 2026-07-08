# Expand/collapse content with Show All/Fewer button

Ensure that clicking 'Show All/Fewer' expands or collapses the relevant content as indicated.

[ClickUp: 868h2319d](https://app.clickup.com/t/868h2319d)

## Analysis

**Status: Verified — no defect found**

- `PillGroupComponent` uses `collapsed` with `.m-pill-group-collapsible.collapsed` (`max-height: 100px`, `overflow-y: hidden` on the outer wrapper) and "Show all N" / "Show fewer" when `pillControls.length > 20`.

## Verification (2026-03-19)

Manual check with a collection that has **30** category pills:

- **Public:** `/collections/187db436-d3e7-43c7-980c-83e143f3df7f` — expand/collapse behaves as expected.
- **Manage:** same UUID `/manage` — same.

Earlier concern (CSS not clipping) does not reproduce in this environment.
