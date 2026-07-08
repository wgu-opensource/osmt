# Phase 6: API and UI Status Display

## Scope of Phase

- Extend SyncIntegrationDto / SyncStateResponse to include statusJson
- SyncController returns statusJson in GET /api/sync/state
- UI: display watermark, status summary, error and correlation ID when present
- Parsed status for structured display (batches completed, last record, error message, correlation ID)

## Code Organization Reminders

- DTOs should be minimal; UI parses statusJson client-side or API returns parsed structure
- One concept per file

## Implementation Details

### 1. API DTO

In SyncController or model:
```kotlin
data class SyncIntegrationDto(
    val syncKey: String,
    val recordType: String,
    val syncWatermark: String?,
    val statusJson: String? = null,  // raw JSON for flexibility
)
```

Or add a parsed `SyncStatusDto` if API should return structured status. Per design, status_json is flexible – returning raw JSON lets UI parse as needed. Alternatively, add optional `status: SyncStatusDto?` when statusJson is present.

### 2. SyncController getSyncState

Map SyncState.statusJson to SyncIntegrationDto.statusJson.

### 3. UI SyncService

Update interface/response type to include statusJson in integrations:
```typescript
export interface SyncIntegrationDto {
  syncKey: string;
  recordType: string;
  syncWatermark: string | null;
  statusJson?: string | null;
}
```

### 4. UI sync-management Component

- Parse statusJson when present: `JSON.parse(i.statusJson)` with try/catch
- Display when no error: batchesCompleted, lastRecordName (optional)
- Display when error: error.message, error.correlationId (with copy button or selectable), recordName
- Add a "Status" or "Last Sync" column/section showing: "Ok" or "Error: {message} (correlation: abc12def34)"
- Correlation ID: make it easy to copy (e.g. in a `<code>` block or with copy icon)

### 5. UI Layout

Options:
- Add column "Status" to table: "Ok" or "Error — abc12def34"
- Or expandable row / details below table for each integration showing status_json details
- Or a single status block when any integration has error

Keep it simple: add Status column with truncated error or "Ok"; full details (correlation ID, record name) in a tooltip or expandable section.

## Tests

- SyncControllerTest or integration test: GET /api/sync/state returns statusJson when set
- sync-management.component.spec: mock response with statusJson, verify error display

## Validate

```bash
sdk env
cd api && mvn test
cd ui && npm run test
cd ui && npm run format:check
```
