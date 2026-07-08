# Phase 1: SyncState Table and Repository

## Scope of Phase

- Create Flyway migration for SyncState table
- Define Exposed SyncStateTable
- Implement SyncStateRepository with CRUD and get-or-create for watermark rows

## Code Organization Reminders

- Prefer granular file structure, one concept per file
- Place more abstract things first
- Keep related functionality grouped
- Any temporary code: TODO comment

## Implementation Details

### 1. Flyway Migration

Create `api/src/main/resources/db/migration/V2026.03.02__sync_state.sql`:

```sql
USE osmt_db;

CREATE TABLE IF NOT EXISTS `SyncState` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `sync_type` VARCHAR(64) NOT NULL,
    `sync_key` VARCHAR(64) NOT NULL,
    `record_type` VARCHAR(64) NOT NULL,
    `sync_watermark` DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sync_state` (`sync_type`, `sync_key`, `record_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. SyncStateTable (Exposed)

Create `api/src/main/kotlin/edu/wgu/osmt/credentialengine/SyncStateTable.kt`:

- LongIdTable("SyncState")
- Columns: syncType, syncKey, recordType, syncWatermark (datetime nullable)
- Unique index on (sync_type, sync_key, record_type)

Reference `AuditLogTable` or `KeywordTable` for style. Use `datetime("sync_watermark").nullable()`.

### 3. SyncStateRepository

Create `api/src/main/kotlin/edu/wgu/osmt/credentialengine/SyncStateRepository.kt`:

- `getWatermark(syncType: String, syncKey: String, recordType: String): LocalDateTime?`
- `updateWatermark(syncType, syncKey, recordType, watermark: LocalDateTime)`
- `getOrCreateRow(syncType, syncKey, recordType)` – insert if not exists, return row
- Use Exposed transactions; `@Repository` or `@Component`

Data class or simple return type for state: `SyncState(syncType, syncKey, recordType, syncWatermark)`.

### 4. Wire SyncStateTable

Add `SyncStateTable` to `ApiServer.kt` tableList so schema checks include it (optional but recommended for consistency).

## Tests

- `SyncStateRepositoryTest.kt`: test getWatermark (null initially), updateWatermark, getOrCreateRow, unique constraint

## Validate

```bash
sdk env
cd api && mvn test -Dtest=SyncStateRepositoryTest
cd api && mvn test
```
