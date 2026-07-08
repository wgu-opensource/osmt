# Count vs Find Query Divergence

**Severity:** Medium
**File:** `SyncQueryHelpers.kt`

## Problem

The "find" and "count" raw JDBC queries use different WHERE clauses for the
same `(watermarkDate, watermarkId)` cursor, which means `getPendingCount` can
return a number that doesn't match what `findSkillsUpdatedSince` will actually
fetch.

### Find query (skills)

```sql
WHERE ((updateDate > ?) OR ((updateDate = ?) AND (id >= ?)))
  AND (publishDate IS NOT NULL)
```

`id >= ?` with `nextId = watermarkId + 1` → effectively `id > watermarkId`.

### Count query (skills)

```sql
WHERE (
  (updateDate > DATE_ADD(?, INTERVAL 1 SECOND))
  OR
  (updateDate BETWEEN DATE_SUB(?, INTERVAL 1 SECOND)
    AND DATE_ADD(?, INTERVAL 1 SECOND) AND id > ?)
)
AND (publishDate IS NOT NULL)
```

The count query uses a **±1 second window** and `id > watermarkId` (no +1
offset). The find query uses **exact timestamp equality** and
`id >= watermarkId + 1`.

## Consequences

1. **Overcounting:** Records with `updateDate` within 1 second *after* the
   watermark but with `id <= watermarkId` are counted but not fetched. The UI
   shows a pending count that never reaches zero.

2. **Undercounting:** Records with `updateDate` exactly equal to the watermark
   (not within the ±1s window) and `id > watermarkId` may be counted
   differently than they're fetched.

3. **UI confusion:** The pending count displayed on the admin sync page won't
   match the number of records actually synced.

## Recommendation

Unify the count and find predicates. The count query should use the same
`(updateDate > ?) OR (updateDate = ? AND id >= ?)` predicate as the find
query (with `nextId = watermarkId + 1`). The 1-second window was added as a
precision safety net, but the find query doesn't use it, so they should be
consistent.
