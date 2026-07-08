# Fix results per page dropdown UI and alignment

Ensure the results per page dropdown displays correctly in the UI and is
properly aligned and visible.

[ClickUp: 868h23199](https://app.clickup.com/t/868h23199)

## Analysis

**Status: Fixed**

- `SizePaginationComponent` renders the "Items per page" label and `<select>`
  (values 50, 100, 150) in `filter-controls`, `collection-public`, and
  `manage-collection`.
- **Root cause**: `filter-section` was a single flex row with
  `justify-content: space-between` and no wrap, so on narrow viewports the
  status checkboxes and pagination control collided (e.g. overlapping text).
  Legacy `.m-choice` layout also misaligned checkboxes vs labels on mobile
  when the filter row used `items-center` on the checkbox group.

## Fix (2026-03-18)

- **`filter-controls.component.html`**: Tailwind on `filter-section`
  (`flex-wrap`, `items-center`, horizontal/vertical gaps) so filters and
  pagination can wrap cleanly on small screens.
- **`l-filterControls`**: `flex flex-wrap gap-x-extraSmall` only (no
  `items-center` on the group) so each `app-filter-choice` aligns naturally.
- **`size-pagination.component.html`**: `items-center whitespace-nowrap` on
  the container so the label and select stay on one line and vertically
  aligned.
- **`filter-choice.component.ts`**: `flex items-center` on `.m-choice` so
  label and checkbox stay vertically centered with the pattern library.

Label / select `for` / `id` association remains optional follow-up (eslint
disable on template); not required for this layout fix.
