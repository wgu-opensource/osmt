# `markSyncInProgress` Correlation ID Is Immediately Discarded

**Severity:** Low
**File:** `SyncService.kt` — `markSyncInProgress()` (line 108), `doSyncSinceWatermark()` (line 154)

## Problem

`SyncController` calls `markSyncInProgress()` before forking to the background:

```kotlin
syncService.markSyncInProgress("default")
ForkJoinPool.commonPool().submit {
    syncService.syncAllSinceWatermark()  // generates a new correlationId
}
```

`markSyncInProgress()` generates correlation ID "abc123" and writes it to `status_json`. Moments later, `syncAllSinceWatermark()` → `doSyncSinceWatermark()` generates a *different* correlation ID "xyz789" and overwrites `status_json`.

## Impact

If the UI polls between `markSyncInProgress` and the first write from `doSyncSinceWatermark`, it shows correlation ID "abc123". Once the sync loop starts, the correlation ID changes to "xyz789". A user who copied "abc123" from the UI and searches logs won't find the actual sync job.

The window is small (milliseconds to seconds) but nonzero, and confusion during incident response is especially costly.

## Fix

Have `markSyncInProgress` accept or return the correlation ID, and pass it to `syncAllSinceWatermark`:

```kotlin
val correlationId = syncService.markSyncInProgress("default")
ForkJoinPool.commonPool().submit {
    syncService.syncAllSinceWatermark("default", correlationId)
}
```

This ensures the same correlation ID appears from the first "in progress" status through to completion.
