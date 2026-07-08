# Phase 2: Fix Backfill Migration and Add Cursor Indexes

## Scope

Fix the `V2026.03.06` backfill migration predicate. Add a new migration with
composite indexes for cursor query performance.

## Code Organization Reminders

- Prefer a granular file structure, one concept per file.
- Place more abstract things, entry points, and tests first.
- Place helper utility functions at the bottom of files.
- Keep related functionality grouped together.
- Any temporary code should have a TODO comment.

## Implementation Details

### 1. Fix `V2026.03.06__sync_state_backfill_last_record_id.sql`

Change `updateDate <= ss.sync_watermark` to `updateDate = ss.sync_watermark`
in both UPDATE statements. The watermark date is the `updateDate` of the last
synced record, so `MAX(id)` among records with that exact timestamp is the
correct cursor position.

Skills update:

```sql
UPDATE SyncState ss
SET ss.last_record_id = (
    SELECT MAX(id) FROM RichSkillDescriptor
    WHERE publishDate IS NOT NULL AND updateDate = ss.sync_watermark
)
WHERE ss.sync_type = 'credential-engine'
  AND ss.sync_key = 'default'
  AND ss.record_type = 'skill'
  AND ss.sync_watermark IS NOT NULL
  AND ss.last_record_id IS NULL;
```

Collections update:

```sql
UPDATE SyncState ss
SET ss.last_record_id = (
    SELECT MAX(id) FROM Collection
    WHERE status IN ('Published', 'Archived') AND updateDate = ss.sync_watermark
)
WHERE ss.sync_type = 'credential-engine'
  AND ss.sync_key = 'default'
  AND ss.record_type = 'collection'
  AND ss.sync_watermark IS NOT NULL
  AND ss.last_record_id IS NULL;
```

### 2. New migration: `V2026.03.09__sync_cursor_indexes.sql`

```sql
USE osmt_db;

CREATE INDEX idx_rsd_update_date_id
    ON RichSkillDescriptor (updateDate, id);

CREATE INDEX idx_collection_update_date_id
    ON Collection (updateDate, id);
```

## Validate

```bash
sdk env install && cd api && mvn test -pl . -Dtest="SyncQueryHelpersTest,SyncServiceTest"
```

Flyway will run the migrations on test startup. Verify no migration errors
in test output.
