# Public Categories

## Design

**Scope:** Make category API and UI routes publicly accessible, aligned with skills and collections. Update CategoryLinksComponent to always show links. Add public-pages documentation.

**Implementation:**
- API: Add CATEGORY_LIST, CATEGORY_DETAIL, CATEGORY_SKILLS to SecurityConfigHelper.configurePublicEndpoints
- API: Add allowPublicLists checks in KeywordController.allCategoriesPaginated and categoryById
- UI: Remove AuthGuard from /categories and /categories/:id routes
- UI: Remove isAuthenticated() gate from CategoryLinksComponent.loadCategoryIds()
- Docs: Add docs/features/2026-03-18-public-pages.md

## Notes

### Questions (resolved)

1. **allowPublicLists** — Used for category list and detail to match skills/collections.
2. **buildAllVersions** — Category endpoints added for all versions (v2, v3, unversioned) for consistency.
3. **CategoryLinksComponent** — Auth gate removed; links now load for all users.
