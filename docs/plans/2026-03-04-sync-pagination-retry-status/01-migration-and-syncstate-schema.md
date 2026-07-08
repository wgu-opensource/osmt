# Phase 1: Migration and SyncState Schema

## Scope of Phase

- Add `status_json` column to SyncState table via Flyway migration
- Update SyncStateTable Exposed definition
- Update SyncState data class and SyncStateRepository to handle status_json

## Code Organization Reminders

- One concept per file
- Place more abstract things first
- Any temporary code: TODO comment

## Implementation Details

### 1. Flyway Migration

Create `api/src/main/resources/db/migration/V2026.03.04__sync_state_status_json.sql`:

```sql
USE osmt_db;

ALTER TABLE `SyncState`
ADD COLUMN `status_json` TEXT NULL
COMMENT 'Job-level sync status: last record, error, correlation ID, progress';
```

### 2. SyncStateTable Update

In `api/src/main/kotlin/edu/wgu/osmt/credentialengine/SyncStateTable.kt`:

- Add `val statusJson = text("status_json").nullable()`
- SyncStateTable is a LongIdTable; add column to object

### 3. SyncState Data Class

Create or update a SyncState data class to include `statusJson: String?`. Used when mapping from SyncStateTable. Check current usage in SyncStateRepository.findAllBySyncKey and getOrCreateRow. Default null for backward compatibility.

### 4. SyncStateRepository

- Add `getStatusJson(syncType, syncKey, recordType): String?`
- Add `updateStatusJson(syncType, syncKey, recordType, statusJson: String)`
- Update findAllBySyncKey to include status_json in returned SyncState
- getOrCreateRow: new rows have status_json null; no change to insert

## Tests

- SyncStateRepositoryTest: verify getStatusJson returns null initially; updateStatusJson persists; findAllBySyncKey includes statusJson

## Validate

```bash
sdk env
cd api && mvn test -Dtest=SyncStateRepositoryTest
cd api && mvn test
```
