# Sync Edge Case Fixes – Notes

## Scope of Work

Fix edge cases and robustness issues in the Credential Engine sync system,
identified during code review. The issues are in `SyncQueryHelpers.kt`,
`SyncService.kt`, `SyncStateRepository.kt`, and a Flyway migration.

## Current State

The sync system uses composite cursor pagination `(watermarkDate, watermarkId)`
to fetch published skills/collections in batches. A raw-JDBC workaround is in
place because Exposed's parameter binding is broken for the cursor query
(see `docs/known-issues/2026-03-04-exposed-sync-cursor-infinite-loop.md`).

The find queries and count queries use different logic, and there are missing
indexes, race conditions, and null-safety gaps.

## Issues

### 1. Count query 1-second window overcounts

**File:** `SyncQueryHelpers.kt` – `countSkillsUpdatedSinceRaw`,
`countCollectionsUpdatedSinceRaw`

The raw count uses a 1-second `DATE_ADD`/`DATE_SUB` window for the
`BETWEEN` clause:

```sql
(updateDate > DATE_ADD(?, INTERVAL 1 SECOND))
OR
(updateDate BETWEEN DATE_SUB(?, INTERVAL 1 SECOND)
  AND DATE_ADD(?, INTERVAL 1 SECOND) AND id > ?)
```

Both source tables and `SyncState.sync_watermark` use `DATETIME(6)`, so there
is no precision mismatch. The window can include records whose `updateDate` is
*before* the watermark but have `id > watermarkId`, producing overcounts.

**Fix:** Use the same predicate as the find query:
`(updateDate > ?) OR (updateDate = ? AND id > ?)`.

### 2. Backfill migration can skip records

**File:** `V2026.03.06__sync_state_backfill_last_record_id.sql`

The backfill sets `last_record_id = MAX(id) WHERE updateDate <= watermark`.
If the watermark came from a batch that didn't process all records sharing that
timestamp, `MAX(id)` may point past unprocessed rows. The next incremental sync
skips them.

**Fix:** Not yet merged. Fix the migration directly: use
`MAX(id) WHERE updateDate = watermark` (exact match on watermark date) instead
of `<= watermark`. This correctly reflects where the cursor stopped.

### 3. `findById!!` NPE if record deleted during sync

**Files:** `SyncQueryHelpers.kt` – `findSkillsUpdatedSinceRaw`,
`findCollectionsUpdatedSinceRaw`

The raw JDBC queries fetch ids, then load entities with
`Dao.findById(id)!!`. If a record is hard-deleted between the SELECT and
the findById, the `!!` throws an NPE.

**Fix:** Use `findById(id)` (nullable) and filter out nulls, with a warning log.

### 4. Missing composite index for cursor ordering

**Tables:** `RichSkillDescriptor`, `Collection`

The cursor queries use `ORDER BY updateDate ASC, id ASC` with a filter on
`publishDate IS NOT NULL` / `status IN (...)`. There is no composite index
covering this. For large tables this can cause filesort.

**Fix:** Add a Flyway migration with composite indexes.

### 5. `getOrCreateRow` insert race

**File:** `SyncStateRepository.kt`

The select-then-insert pattern in `getOrCreateRow` is not atomic. Two concurrent
callers can both see null and both INSERT, hitting the unique constraint.

**Status:** Dropped. Can't happen with current call graph. Documented as
single-instance limitation in feature docs.

### 6. Collection deduplication not logged

**File:** `SyncService.kt`

Skill dedup logs a warning but collection dedup is silent.

**Fix:** Add the same log.warn for collections.

## Questions

### Q1: Should we fix the backfill migration directly?

**Context:** The migration `V2026.03.06` hasn't merged yet. It uses
`MAX(id) WHERE updateDate <= watermark` which can overshoot.

**Answer:** Yes. Fix in place: use `MAX(id) WHERE updateDate = watermark`.

### Q2: Which composite indexes should we add?

**Context:** The cursor queries filter on `publishDate IS NOT NULL` for skills
and `status IN ('Published', 'Archived')` for collections, then order by
`(updateDate, id)`.

**Answer:** Add two indexes in one migration:
- `RichSkillDescriptor(updateDate, id)`
- `Collection(updateDate, id)`

### Q3: How should we handle the `getOrCreateRow` race?

**Answer:** Dropped from plan. The race can't occur with the current call graph
(AtomicBoolean prevents concurrent sync-all, single-record sync doesn't call
getOrCreateRow). Added single-instance limitation to feature docs instead.
