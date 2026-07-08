# Partial Batch Failure Loses Progress

**Severity:** Medium
**File:** `SyncService.kt`

## Problem

The watermark is advanced only after an entire batch succeeds. If a batch of
20 records processes 15 successfully and then fails on record 16, the watermark
is not advanced. On retry, all 20 records (including the 15 already-published
ones) are re-fetched and re-synced.

Relevant code path in `doSyncSinceWatermark`:

```kotlin
val result = processSkillBatch(target, dedupedBatch, ...)
result.fold(
    onSuccess = { },
    onFailure = { return Result.failure(it) },  // ← exits without advancing watermark
)
// watermark advance happens here, only on success
syncStateRepository.updateWatermark(...)
```

## Consequences

1. **Wasted CE API calls:** 15 records are re-published. CE handles these
   idempotently, but it wastes rate-limited API quota and adds latency.

2. **Thundering retry:** If the 16th record keeps failing (e.g., invalid data
   that CE rejects), every retry attempt re-publishes the same 15 good records
   before hitting the same failure. With retries at the record level (5
   attempts × 15 records = 75 extra CE calls per sync attempt), this adds up.

3. **Permanently stuck sync:** If the failing record can't be fixed (e.g., it
   has data that CE will never accept), the sync is permanently stuck at this
   batch. The only escape is manual intervention (fix the data or advance the
   watermark manually).

## Recommendation

Advance the watermark per-record instead of per-batch. After each successful
individual publish, update the watermark to that record's `(updateDate, id)`.
This way, on failure, only the failing record and those after it are retried.

Alternatively, implement a "poison pill" mechanism: after N consecutive
failures on the same record, skip it (log an error, record it in status JSON)
and advance the watermark past it.
