# Per-Record Progress Updates Drop `inProgress=true`

**Severity:** Critical
**File:** `SyncService.kt` — `processSkillBatch()` (lines 501–515), `processCollectionBatch()` (lines 572–586)

## Problem

At the start of `doSyncSinceWatermark`, the status is correctly set to `inProgress = true`. But inside the per-record loop (`processSkillBatch` / `processCollectionBatch`), each successful record writes a progress update **without** `inProgress = true`:

```kotlin
val progress = SyncStatusJson(
    lastRecordUuid = dao.uuid,
    lastRecordName = dao.name,
    batchIndex = batchIndex,
    batchesCompleted = batchIndex,
    lastUpdatedAt = nowIso(),
    sessionCorrelationId = sessionCorrelationId,
    // inProgress is NOT set — defaults to null
)
syncStateRepository.updateStatusJson(...)
```

`SyncStatusJson.inProgress` defaults to `null`. With `@JsonInclude(NON_NULL)`, the serialized JSON omits the field entirely.

## Impact

The UI's `isSyncDone()` method checks:

```typescript
return s?.inProgress !== true;
```

When `inProgress` is `null`/`undefined`, this evaluates to `true` — "sync is done."

This means:

1. User triggers sync → UI shows "In progress…" briefly
2. First record publishes → progress update overwrites status → `inProgress` is absent
3. UI polls → `isSyncDone()` returns `true` → shows "Sync completed successfully"
4. Auto-refresh stops
5. Sync is actually still running in the background

The user gets a false "done" signal after the very first record.

## Fix

Add `inProgress = true` to the progress update in both `processSkillBatch` and `processCollectionBatch`:

```kotlin
val progress = SyncStatusJson(
    lastRecordUuid = dao.uuid,
    lastRecordName = dao.name,
    batchIndex = batchIndex,
    batchesCompleted = batchIndex,
    lastUpdatedAt = nowIso(),
    sessionCorrelationId = sessionCorrelationId,
    inProgress = true,  // <-- add this
)
```

This keeps the "in progress" signal consistent until the final status write at the end of `doSyncSinceWatermark`, which correctly sets `inProgress = false`.
