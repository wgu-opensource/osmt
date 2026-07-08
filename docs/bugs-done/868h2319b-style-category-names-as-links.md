# Style category names as links

Update the UI so that category names are visually styled as links.

[ClickUp: 868h2319b](https://app.clickup.com/t/868h2319b)

## Analysis

**Status: Fixed**

- Category names appear in several places: skill list row (`getFormattedCategories()`), collection pill group (`KeywordCountPillControl`), rich skill detail card, etc.
- In `skill-list-row.component.html`, categories are plain text in `m-inlineHeading-x-text`. Skill names use `<a class="t-focus t-type-bodyLink" [routerLink]="...">`.
- `KeywordCountPillControl.primaryLabel` returns `${this.keyword}` as string; pills render this as text.
- Category detail route exists: `/categories/:id`. Category names could link there.

**Root cause**: Category names are rendered as text; they should use `<a>` with `routerLink` to `/categories/:id` and link styling (e.g. `t-type-bodyLink`, underline, hover).

## Fix (2026-03-18)

- **API**: `RichSkillDescriptor.getKeywordsFromSkills` now returns `ApiNamedReference(id, name)` for categories instead of raw strings, so collection `skillKeywords` includes category ids.
- **Pills**: `KeywordCountPillControl` accepts optional `categoryLinkPrefix`; when set and keyword has id, pill renders as link. Collection views pass `'/categories/'`.
- **Skill lists**: Added `CategoryLinksComponent` that fetches category id map and renders each category name as `routerLink` when id is found. Used in `skill-list-row` and `public-table`.
