# Draft and Deleted Records Silently Skipped

**Severity:** Low
**Files:** `SyncService.kt`, `SyncQueryHelpers.kt`

## Problem

The find queries filter by `publishDate IS NOT NULL` (skills) or
`status IN ('Published', 'Archived')` (collections). However, the sync
processing logic in `syncOneSkillWithRetry` / `syncOneCollectionWithRetry`
has an `else -> Result.success(Unit)` branch for non-Published/non-Archived
statuses.

This means if a record's status changes between the query and the sync call
(e.g., an admin archives then un-archives a skill while sync is running), the
record can pass the DB filter but reach the `else` branch. The sync returns
success without publishing, the watermark advances past it, and the record is
never synced.

### Skills: `publishDate IS NOT NULL` includes both Published and Archived

For skills, status is derived from `publishDate`/`archiveDate`:
- **Published:** `publishDate != null && archiveDate == null`
- **Archived:** `publishDate != null && archiveDate != null`
- **Deleted:** `publishDate == null && archiveDate != null` → excluded by query ✓
- **Draft:** both null → excluded by query ✓

So a skill that was Published and then moved to Draft (setting
`publishDate = null`) mid-sync would not be re-fetched by the query—but would
already be in the current batch. `publishStatus()` would return `Draft`, and
the `else` branch would silently succeed.

### Collections: `status IN ('Published', 'Archived')` is explicit

A collection moved to `Draft` or `Workspace` mid-sync has the same issue.

## Impact

Low in practice. The race window is small (within a single batch processing
time). The record will be picked up on the next sync if it returns to a
publishable state. But if it stays in Draft permanently, it remains published
in CE with stale data.

## Recommendation

Log a warning when hitting the `else` branch during batch sync so operators
know a record was skipped. Consider whether Draft/Deleted records that were
previously synced should be explicitly removed or deprecated in CE.
