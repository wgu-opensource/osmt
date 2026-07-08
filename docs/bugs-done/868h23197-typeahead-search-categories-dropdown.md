# Show all relevant results in type-ahead search for categories dropdown

Type-ahead search in categories dropdown should display all relevant results.

[ClickUp: 868h23197](https://app.clickup.com/t/868h23197)

## Analysis

**Status: Fixed**

- `CustomKeywordRepositoryImpl.typeAheadSearch` in
  `api/src/main/kotlin/edu/wgu/osmt/keyword/KeywordEsRepo.kt` used
  `OffsetPageable(0, 20, null)` when `searchStr.isNotEmpty()`.
- When empty, it returns up to 10,000. With a query, only 20 results were
  returned.
- `CustomJobCodeRepositoryImpl` used the same pattern:
  `limit = if (searchStr.isEmpty()) 10000 else 20`.
- UI uses `KeywordSearchService.searchKeywords()` → `search/jobcodes` or
  `search/keywords` with `query` and `type` params.

**Root cause**: Hard limit of 20 when the user types in the type-ahead, so
relevant results beyond the first 20 were hidden.

## Fix (2026-03-19)

- Shared constant `TYPEAHEAD_NON_EMPTY_QUERY_MAX_RESULTS` (100) in
  `api/src/main/kotlin/edu/wgu/osmt/elasticsearch/TypeAheadSearchLimits.kt`.
- Keyword and job-code type-ahead use it for non-empty queries; empty-query
  behavior unchanged (10,000).

## Deferred UX (not addressed in this ticket)

The keyword / category multi-select type-ahead control still has issues that
were noted during investigation but intentionally left out of scope:

- **Keyboard navigation**: No arrow-up / arrow-down / Enter to move and
  confirm selection in the suggestion list.
- **Visual stability**: The control can flash or jump when results refresh.
- **Layout**: The dropdown occupies in-flow space and can trigger page
  relayout as it opens or resizes.

Follow-up work would live in a separate UI / accessibility story.
