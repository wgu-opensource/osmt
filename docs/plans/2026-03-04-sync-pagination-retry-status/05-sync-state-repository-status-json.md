# Phase 5: SyncStateRepository status_json

## Scope of Phase

- Verify getStatusJson and updateStatusJson from Phase 1 are complete and correct
- Ensure getOrCreateRow initializes status_json as null for new rows
- Wire status_json into SyncState model returned by findAllBySyncKey
- Add any missing tests or edge case handling

## Code Organization Reminders

- One concept per file
- Repository methods should be transactional

## Implementation Details

### 1. SyncState Model

Ensure SyncState (or equivalent) has `statusJson: String?`. Used when returning from findAllBySyncKey. If SyncState is a data class used in SyncController, add the field.

### 2. getStatusJson

```kotlin
fun getStatusJson(syncType: String, syncKey: String, recordType: String): String? =
    SyncStateTable
        .select { ... }
        .firstOrNull()
        ?.get(SyncStateTable.statusJson)
```

### 3. updateStatusJson

```kotlin
fun updateStatusJson(syncType: String, syncKey: String, recordType: String, statusJson: String) {
    SyncStateTable.update({ ... }) {
        it[SyncStateTable.statusJson] = statusJson
    }
}
```

Note: Row must exist. SyncService calls getOrCreateRow before updateStatusJson, so row exists.

### 4. findAllBySyncKey

Update to select status_json and include in returned SyncState:
```kotlin
SyncState(
    ...
    statusJson = it[SyncStateTable.statusJson],
)
```

### 5. SyncState Data Class

If SyncState is defined in SyncStateRepository or elsewhere:
```kotlin
data class SyncState(
    val syncType: String,
    val syncKey: String,
    val recordType: String,
    val syncWatermark: LocalDateTime?,
    val statusJson: String? = null,
)
```

## Tests

- SyncStateRepositoryTest: getStatusJson returns null for new row; updateStatusJson then getStatusJson returns value; findAllBySyncKey includes statusJson

## Validate

```bash
sdk env
cd api && mvn test -Dtest=SyncStateRepositoryTest
cd api && mvn test
```
