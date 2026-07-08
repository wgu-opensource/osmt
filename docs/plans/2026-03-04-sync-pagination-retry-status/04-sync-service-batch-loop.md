# Phase 4: SyncService Batch Loop

## Scope of Phase

- Refactor doSyncSinceWatermark to process in batches
- Update watermark after each successful batch
- On record failure after retries: update status_json, STOP (return without advancing)
- Use SyncRetryHelper for each record
- Update status_json with progress after each batch; with error on failure

## Code Organization Reminders

- Place orchestration logic in SyncService
- Delegate to SyncRetryHelper, SyncStatusJson
- One concept per file; helpers at bottom

## Implementation Details

### 1. Inject Config

SyncService needs batch size and retry config. Options:
- Add SyncConfig @ConfigurationProperties or @Value for batch-size, retry-attempts, retry-initial-delay-ms, retry-delay-multiplier
- Or inject individual @Value in SyncService

### 2. Batch Loop (doSyncSinceWatermark)

```
getOrCreateRow
watermark = getWatermark
batchIndex = 0
batchesCompleted = 0
while (true) {
  batch = findXxxUpdatedSince(watermark, batchSize)
  if (batch.isEmpty()) break
  for (dao in batch) {
    result = SyncRetryHelper.withRetry(...) { syncOne(dao) }
    if (result.isFailure) {
      status = SyncStatusJson(error = SyncStatusError(...))
      updateStatusJson(status)
      return Result.failure  // STOP
    }
    updateStatusJson(progress for this batch)
  }
  maxDate = batch.maxOf { it.updateDate }
  updateWatermark(maxDate)
  batchesCompleted++
  batchIndex++
}
return Result.success
```

### 3. Stop on Failure

When `withRetry` returns failure:
- Build SyncStatusError with correlationId, message, recordUuid, recordName, occurredAt
- Build SyncStatusJson with error and lastRecord info
- updateStatusJson(serialize(status))
- Return Result.failure(...) – do NOT update watermark

### 4. Progress Updates

After each successful batch (or each record if desired for finer progress):
- updateStatusJson with lastRecordUuid, lastRecordName, batchIndex, batchesCompleted, lastUpdatedAt
- Clear error field when progressing (optional: only set error on failure)

### 5. syncAllSinceWatermark Order

Skills first, then collections. Each record type has its own status_json row (per sync_key, record_type). Failure in skills stops before collections; failure in collections stops that type only.

### 6. SyncRetryHelper Integration

Wrap each SyncTarget call:
```kotlin
val result = syncRetryHelper.withRetry(attempts, initialDelayMs, multiplier) {
    when (rsd.publishStatus()) {
        PublishStatus.Published -> target.publishSkill(rsd)
        PublishStatus.Archived -> target.deprecateSkill(rsd)
        else -> Result.success(Unit)
    }
}
```

## Tests

- SyncServiceTest: batch processing with small batch size; watermark advances per batch; on failure, watermark not advanced, status_json has error; retry helper called (mock)
- Integration: MockSyncTarget, verify batches; force failure on 3rd record, assert sync stops

## Validate

```bash
sdk env
cd api && mvn test -Dtest=SyncServiceTest
cd api && mvn test
```
