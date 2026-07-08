# Credential Engine Sync – Pagination, Retry, Status

## Scope of Work

- **Pagination**: Batch processing (default 20 records) with cursor-based pagination on `updateDate`.
- **Incremental watermark**: Update watermark after each successful batch. Stop on first record failure.
- **Retry**: 5 attempts per record, exponential backoff (5s × 1.5^attempt), capped at 60s. Retry count capped at 10.
- **status_json**: On SyncState – job-level debugging: last record, error, correlation ID (alphanumeric), batch progress. On failure: stop, record error, next sync retries from same watermark.
- **UI**: Display watermark, status from status_json (error, correlation ID, progress).

## File Structure

```
api/src/main/
├── kotlin/edu/wgu/osmt/credentialengine/
│   ├── SyncStateTable.kt              # UPDATE: add status_json column
│   ├── SyncStateRepository.kt         # UPDATE: get/update status_json
│   ├── SyncService.kt                 # UPDATE: batch loop, retry, watermark per batch, stop on failure
│   ├── SyncQueryHelpers.kt            # UPDATE: add limit param, cursor pagination
│   ├── SyncRetryHelper.kt             # NEW: exponential backoff, fail-safe caps
│   └── SyncStatusJson.kt              # NEW: data class + serialization for status_json
├── resources/
│   ├── config/
│   │   └── application.properties     # UPDATE: batch-size, retry config
│   └── db/migration/
│       └── V2026.03.04__sync_state_status_json.sql  # NEW: add status_json

ui/src/app/admin/sync/
├── sync-management.component.ts       # UPDATE: display status_json fields
├── sync-management.component.html    # UPDATE: show error, correlation ID
└── sync.service.ts                    # UPDATE: SyncIntegrationDto has statusJson
```

## Conceptual Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ SyncController  POST /api/sync/all → ForkJoinPool.submit(syncAll...)         │
│                 GET /api/sync/state → SyncStateRepository + status_json      │
└──────────────────────────────────────────┬──────────────────────────────────┘
                                           │
                                           ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ SyncService.syncAllSinceWatermark()                                          │
│   For each record type (skill, collection):                                  │
│     while (batch = fetchBatch(watermark, limit)) {                           │
│       for each record in batch:                                              │
│         result = SyncRetryHelper.withRetry { target.publish(record) }        │
│         if (failure) → updateStatusJson(error, record, correlationId)        │
│                    → STOP (don't advance watermark)                           │
│       updateWatermark(maxDate)  # success only                                │
│       updateStatusJson(progress)                                             │
│     }                                                                        │
└──────────────────────────────────────────┬──────────────────────────────────┘
                                           │
            ┌──────────────────────────────┼──────────────────────────────┐
            ▼                              ▼                              ▼
┌─────────────────────┐    ┌───────────────────────────┐    ┌─────────────────────┐
│ SyncQueryHelpers    │    │ SyncStateRepository        │    │ SyncRetryHelper      │
│ findXxxUpdatedSince │    │ getWatermark, update       │    │ exponential backoff  │
│ (watermark, limit)  │    │ status_json                │    │ fail-safe: ≤10, ≤60s │
└─────────────────────┘    └───────────────────────────┘    └─────────────────────┘
```

## status_json Shape

```json
{
  "lastRecordUuid": "uuid",
  "lastRecordName": "Skill name",
  "batchIndex": 1,
  "batchesCompleted": 5,
  "lastUpdatedAt": "2026-03-04T12:00:00Z",
  "error": {
    "message": "CE publish failed: 503",
    "correlationId": "abc12def34",
    "recordUuid": "uuid",
    "recordName": "Skill name",
    "occurredAt": "2026-03-04T12:05:00Z"
  }
}
```

When no error: `error` absent or null. `correlationId` alphanumeric (e.g. 10 chars), for log search.

## Main Components

| Component | Responsibility |
|-----------|----------------|
| SyncStateTable | Add `status_json` TEXT/JSON column |
| SyncStateRepository | `getStatusJson`, `updateStatusJson` (merge or replace) |
| SyncStatusJson | Data class for status shape; Jackson serialization |
| SyncRetryHelper | `withRetry(attempts, block)` – exponential backoff, caps |
| SyncQueryHelpers | Add `limit` param; `findSkillsUpdatedSince(watermark, limit)` |
| SyncService | Batch loop; retry per record; update watermark per batch; on failure: update status_json, return/throw (stop) |
| SyncController | Unchanged flow; DTO includes statusJson |
| UI | Display watermark, status summary, error + correlation ID when present |
