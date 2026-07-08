# Credential Engine Sync – Pagination, Retry, Per-Record Status

## Scope of Work

- **Pagination**: Process records in configurable batches (e.g. 50–200) instead of loading all at once. Use cursor-based pagination on `updateDate`.
- **Incremental watermark**: Update watermark after each successful batch, not only at end of full sync. Enables progress visibility in UI.
- **Retry**: Retry each record N times (default 5) with exponential backoff (5s × 1.5^attempt, capped at 60s). After N failures, stop sync, record error in status_json. Next sync restarts from same watermark.
- **status_json**: Column on SyncState for job-level debugging: last record tried, error, error correlation ID (alphanumeric, for log hunting), batch progress, etc. Flexible JSON for evolving needs. No per-record table for now.
- **UI**: Display status from status_json (last error, correlation ID, progress), and watermark advancing.

## Current State of Codebase

### SyncService
- `doSyncSinceWatermark()` fetches all records via `findSkillsUpdatedSince` / `findCollectionsUpdatedSince` (no LIMIT).
- Processes entire list in memory; updates watermark once at end with `recordsAndDates.maxOrNull()`.
- No retry; failures throw and abort the sync.
- `processSkills` / `processCollections` call target per-record; CE API is one request per record.

### SyncQueryHelpers
- `findSkillsUpdatedSince(watermark)`: single query, no limit, `orderBy(updateDate)`.
- `findCollectionsUpdatedSince(watermark)`: same pattern.
- Cursor-friendly: `updateDate > watermark ORDER BY updateDate ASC` already in place; needs `LIMIT batchSize`.

### SyncState
- Table: `sync_type`, `sync_key`, `record_type`, `sync_watermark`.
- One row per (syncKey, recordType). No per-record status, no status_json.
- SyncStateRepository: getWatermark, updateWatermark, getOrCreateRow, findAllBySyncKey.

### SyncController
- POST /api/sync/all: fires `syncAllSinceWatermark()` in ForkJoinPool, returns 202.
- GET /api/sync/state: returns integrations with watermark strings.
- Response DTO: SyncIntegrationDto(syncKey, recordType, syncWatermark).

### UI (sync-management)
- Table: Integration Key, Record Type, Watermark.
- "Sync Now" button. No failure display, no correlation IDs, no progress indication.

### CredentialEngineSyncTarget
- One HTTP POST per skill/collection. No batch API used.

## Questions

### Q1. Where does status_json live?
**Context**: User wants status_json for debugging (error, last record tried, etc.).

**Answer**: SyncState only. status_json on SyncState holds job-level status: last record attempted, error, error correlation ID, batch progress, etc. No per-record table for now – avoids added complexity. May add per-record state later if needed.

### Q2. Failed records – advance watermark or not?
**Context**: If a record fails after retries, what do we do?

**Answer**: Stop the sync. Do NOT advance the watermark past the failed record. Record the error and failing record in status_json (with correlation ID). Next time we "sync all", we start from the same watermark – the failed record is first in the queue and we retry it. Sync stops on first unrecoverable failure; no continuing past it.

### Q3. Batch size configuration?
**Context**: Need configurable batch size.

**Answer**: `credential-engine.sync.batch-size` (default 20). Overridable via `CREDENTIAL_ENGINE_SYNC_BATCH_SIZE` env var.

### Q4. Retry count and backoff?
**Context**: Retry N times with backoff.

**Answer**: Retry count default 5. Exponential backoff: start 5s, power 1.5. Delays per attempt: 5, 7.5, 11.25, 16.875, 25.3125 seconds (attempts 0–4). Config: `credential-engine.sync.retry-attempts`, `credential-engine.sync.retry-initial-delay-ms` (5000). Fail-safe: cap retry count at 10, cap delay at 60s to avoid runaway behavior.

## Notes

- Per-record state (SyncRecordResult) deferred to avoid complexity. status_json on SyncState is sufficient for v1. Can add per-record table later if needed.
