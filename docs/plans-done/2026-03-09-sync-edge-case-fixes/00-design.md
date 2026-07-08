# Sync Edge Case Fixes – Design

## Scope of Work

Fix edge cases in the Credential Engine sync: count query overcounting,
backfill migration skipping records, null-safety in raw JDBC entity loading,
missing indexes, and silent collection dedup.

## File Structure

```
api/src/
├── main/
│   ├── kotlin/edu/wgu/osmt/credentialengine/
│   │   ├── SyncQueryHelpers.kt          # UPDATE: fix count queries, null-safe findById
│   │   └── SyncService.kt              # UPDATE: log collection dedup
│   └── resources/db/migration/
│       ├── V2026.03.06__sync_state_backfill_last_record_id.sql  # UPDATE: fix MAX(id) predicate
│       └── V2026.03.09__sync_cursor_indexes.sql                 # NEW: composite indexes
└── test/kotlin/edu/wgu/osmt/credentialengine/
    ├── SyncQueryHelpersTest.kt          # UPDATE: add count-accuracy test
    └── SyncServiceTest.kt              # existing tests validate no regressions

docs/features/
└── 2026-03-03-credential-engine-sync.md # UPDATE: added Known Limitations
```

## Architecture

No new components. Fixes target existing query logic and migrations.

```
Count queries ──┐
                ├── Use same predicate as find: (date > ?) OR (date = ? AND id > ?)
Find queries  ──┘

findById!! ────── Replace with findById + filterNotNull + log.warn

Backfill migration ── WHERE updateDate = watermark (not <=)

New migration ──── CREATE INDEX (updateDate, id) on RichSkillDescriptor, Collection
```

## Main Components

| Component | Change |
|---|---|
| `countSkillsUpdatedSinceRaw` | Replace 1-second window with exact composite cursor predicate |
| `countCollectionsUpdatedSinceRaw` | Same |
| `findSkillsUpdatedSinceRaw` | `findById` → nullable + filterNotNull + warn |
| `findCollectionsUpdatedSinceRaw` | Same |
| `SyncService.doSyncSinceWatermark` | Log collection dedup warnings |
| `V2026.03.06` migration | `updateDate = watermark` instead of `<= watermark` |
| `V2026.03.09` migration (new) | Composite indexes for cursor ordering |
