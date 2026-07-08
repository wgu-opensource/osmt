# Phase 3: Cleanup and Validation

## Scope

Log collection dedup in SyncService, grep for leftover TODOs or debug code,
run full test suite, fix warnings/formatting.

## Code Organization Reminders

- Prefer a granular file structure, one concept per file.
- Place more abstract things, entry points, and tests first.
- Place helper utility functions at the bottom of files.
- Keep related functionality grouped together.
- Any temporary code should have a TODO comment.

## Implementation Details

### 1. Log collection dedup in `SyncService.kt`

In `doSyncSinceWatermark`, the collection dedup block silently filters
duplicates. Add the same warning log that skills already have:

```kotlin
SyncRecordType.COLLECTION -> {
    val colls = cursorFilteredBatch as List<CollectionDao>
    val seen = mutableSetOf<String>()
    val deduped = colls.filter { seen.add(it.uuid) }
    if (deduped.size < colls.size) {
        log.warn(
            "[{}] Batch {} dropped {} duplicate collections",
            sessionCorrelationId,
            batchIndex,
            colls.size - deduped.size,
        )
    }
    deduped
}
```

### 2. Remove stale comments

Remove the comment in `countSkillsUpdatedSinceRaw` and
`countCollectionsUpdatedSinceRaw` about the 1-second window (no longer
applicable after Phase 1 changes).

### 3. Grep for leftover issues

```bash
git diff --cached --diff-filter=ACMR | grep -E 'TODO|FIXME|HACK|XXX|debug'
```

Remove any that won't be addressed in a later phase.

## Validate

```bash
sdk env install
cd api && mvn test
cd ../ui && npm run format:check
```

Fix all warnings, errors, and formatting issues.

## Plan Cleanup

Add a summary of completed work to `docs/plans/2026-03-09-sync-edge-case-fixes/summary.md`.

## Commit

```
fix(sync): fix count query overcounting and cursor edge cases

- Align count queries with find query predicate: (date > ?) OR (date = ? AND id > ?)
- Fix backfill migration to use updateDate = watermark instead of <=
- Add composite indexes on (updateDate, id) for cursor performance
- Null-safe findById in raw JDBC queries with warning log
- Log collection dedup warnings (was silent, skills already logged)
- Document single-instance sync limitation
```
