# Sync Edge Case Fixes – Summary

## Completed Work

### Phase 1: Fix Count Queries and Null-Safe findById

- **Count queries:** Replaced 1-second window in `countSkillsUpdatedSinceRaw` and
  `countCollectionsUpdatedSinceRaw` with exact composite cursor predicate:
  `(updateDate > ?) OR (updateDate = ? AND id > ?)` to match find query logic.
- **Null-safe loading:** Replaced `findById(...)!!` with `mapNotNull` and
  `findById(...).also { log.warn if null }` in both raw JDBC functions to avoid
  NPE when a record is deleted between SELECT and load.
- **Test:** Added `countSkillsUpdatedSince matches find count after partial sync`.

### Phase 2: Fix Backfill Migration and Add Indexes

- **Backfill:** Changed `V2026.03.06` from `updateDate <= watermark` to
  `updateDate = watermark` so we don't overshoot and skip records.
- **Indexes:** Added `V2026.03.09__sync_cursor_indexes.sql` with
  `(updateDate, id)` on RichSkillDescriptor and Collection.

### Phase 3: Cleanup and Validation

- **Collection dedup logging:** Added `log.warn` when duplicate collection uuids
  are dropped in a batch (skills already had this).
- **Feature docs:** Added Known Limitations (single-instance sync) to
  `docs/features/2026-03-03-credential-engine-sync.md`.

## Files Changed

- `api/src/main/kotlin/edu/wgu/osmt/credentialengine/SyncQueryHelpers.kt`
- `api/src/main/kotlin/edu/wgu/osmt/credentialengine/SyncService.kt`
- `api/src/main/resources/db/migration/V2026.03.06__sync_state_backfill_last_record_id.sql`
- `api/src/main/resources/db/migration/V2026.03.09__sync_cursor_indexes.sql` (new)
- `api/src/test/kotlin/edu/wgu/osmt/credentialengine/SyncQueryHelpersTest.kt`
- `docs/features/2026-03-03-credential-engine-sync.md`
