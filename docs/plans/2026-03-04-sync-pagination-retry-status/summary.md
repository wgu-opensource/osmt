# Sync Pagination, Retry, Status – Implementation Summary

## Summary of Changes

- **status_json** on SyncState for job-level status and error tracking
- Batch processing (default 20 records) with incremental watermark updates
- Retry 5× with exponential backoff (5s × 1.5^attempt, max 60s delay)
- On first record failure: stop sync, record error and correlation ID in status_json
- UI displays status, error message, and copyable correlation ID

## New Files

- `api/.../credentialengine/SyncStatusJson.kt` – status data class and JSON serialization
- `api/.../credentialengine/CorrelationId.kt` – generate correlation IDs
- `api/.../credentialengine/SyncRetryHelper.kt` – retry with backoff
- `api/src/main/resources/db/migration/V2026.03.04__sync_state_status_json.sql` – migration

## Modified Files

### API

- `SyncStateTable.kt` – added `statusJson` column
- `SyncState.kt` – added `statusJson` field
- `SyncStateRepository.kt` – `getStatusJson`, `updateStatusJson`, `findAllBySyncKey` includes statusJson
- `SyncService.kt` – batch loop, `processSkillBatch`, `processCollectionBatch`, retry per record
- `SyncQueryHelpers.kt` – `findSkillsUpdatedSince`, `findCollectionsUpdatedSince` with limit
- `SyncController.kt` – `SyncIntegrationDto` includes `statusJson`
- `application.properties` – new config properties

### UI

- `sync.service.ts` – `SyncIntegrationDto` includes `statusJson`
- `sync-management.component.ts` – `getStatusDisplay`, `copyToClipboard`
- `sync-management.component.html` – Status column with error/correlation ID
- `sync-management.component.scss` – `.m-code-copyable` styles
- `sync-management.component.spec.ts` – tests for getStatusDisplay

## Config Properties

```
credential-engine.sync.batch-size=20
credential-engine.sync.retry-attempts=5
credential-engine.sync.retry-initial-delay-ms=5000
credential-engine.sync.retry-delay-multiplier=1.5
```

## Migration

- `V2026.03.04__sync_state_status_json.sql` – adds `status_json` TEXT column to SyncState
