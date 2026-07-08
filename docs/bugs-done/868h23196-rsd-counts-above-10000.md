# Report total RSD counts above 10,000 accurately

Ensure that total RSD counts above 10,000 are accurately reported.

[ClickUp: 868h23196](https://app.clickup.com/t/868h23196)

## Analysis

**Status: Fixed**

Elasticsearch **search** responses default to **`track_total_hits` = 10,000**,
so **`SearchHits.totalHits`** is not exact above that threshold. Accurate totals
use **`ElasticsearchTemplate.count()`** (or equivalent **`countByApiSearch`**).

## Fix (2026-03-19)

- **`PaginatedLinks`**: Use the same exact count as **`X-Total-Count`** —
  **`SearchController.searchSkills`**, **`HasAllPaginated`**, and
  **`KeywordController.searchRelatedSkills`** now pass **`countByApiSearch` /
  `countAllFilteredByPublishStatus`** into **`PaginatedLinks`**, not
  **`searchHits.totalHits`**.
- **`RichSkillController`** filtered skills: **`countByApiSearch`** for header
  and **`PaginatedLinks`** (mirrors skill search).
- **Collections**: **`CustomCollectionQueries.countByApiSearch`** — refactored
  **`matchingCollectionUuids`** shared with **`byApiSearch`**; final cardinality
  via **`count`** on the terms query over collection ids. **`SearchController
  .searchCollections`** uses it for header and **`PaginatedLinks`**.
- **Batch tasks**: **`RichSkillRepository.changeStatusesForTask`**,
  **`CollectionRepository`** (skill add/remove by search, collection status
  change by search) use **`countByApiSearch`** for **`ApiBatchResult.totalCount`**.

**Tests**: **`SearchControllerTest`** asserts **`X-Total-Count`** matches
**`countByApiSearch`** for collection and skill search (same params as the
controller).

## Historical note (pre-fix)

Several endpoints used **`count()`** for **`X-Total-Count`** but **`searchHits
.totalHits`** for **`Link`** headers, so pagination metadata could disagree with
the header above ~10k matches.
