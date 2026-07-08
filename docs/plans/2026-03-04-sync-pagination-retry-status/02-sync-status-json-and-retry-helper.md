# Phase 2: SyncStatusJson and SyncRetryHelper

## Scope of Phase

- SyncStatusJson: data class for status_json shape; serialization
- SyncRetryHelper: exponential backoff, fail-safe caps (retry ≤10, delay ≤60s)
- Generate alphanumeric correlation ID for error tracking

## Code Organization Reminders

- One concept per file
- Place more abstract things first
- Helper utilities at bottom of files

## Implementation Details

### 1. SyncStatusJson

Create `api/src/main/kotlin/edu/wgu/osmt/credentialengine/SyncStatusJson.kt`:

```kotlin
data class SyncStatusJson(
    val lastRecordUuid: String? = null,
    val lastRecordName: String? = null,
    val batchIndex: Int? = null,
    val batchesCompleted: Int? = null,
    val lastUpdatedAt: String? = null,  // ISO-8601
    val error: SyncStatusError? = null,
)

data class SyncStatusError(
    val message: String,
    val correlationId: String,
    val recordUuid: String?,
    val recordName: String?,
    val occurredAt: String,  // ISO-8601
)
```

- `fun SyncStatusJson.toJsonString(): String` – ObjectMapper.writeValueAsString
- `fun parseSyncStatusJson(json: String?): SyncStatusJson?` – null-safe, return default if parse fails
- Use Jackson ObjectMapper (from Spring or inject)

### 2. Correlation ID

Add to SyncStatusJson.kt or a small util:

```kotlin
fun generateCorrelationId(): String {
    val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
    return (1..10).map { chars.random() }.joinToString("")
}
```

Use `java.security.SecureRandom` or `kotlin.random.Random` for alphanumeric only.

### 3. SyncRetryHelper

Create `api/src/main/kotlin/edu/wgu/osmt/credentialengine/SyncRetryHelper.kt`:

- Inject or accept: `retryAttempts`, `initialDelayMs`, `delayMultiplier` (1.5)
- Fail-safe: cap `retryAttempts` at 10; cap computed delay at 60_000 ms
- `fun <T> withRetry(attempts: Int, initialDelayMs: Long, delayMultiplier: Double, block: () -> Result<T>): Result<T>`
- On failure: Thread.sleep(delay) then retry; delay = min(initialDelayMs * multiplier^attempt, 60_000)
- Return first success; on final failure return Result.failure

Example usage:
```kotlin
SyncRetryHelper.withRetry(5, 5000, 1.5) { target.publishSkill(rsd) }
```

### 4. Configuration

Add to `application.properties`:
```
credential-engine.sync.batch-size=20
credential-engine.sync.retry-attempts=5
credential-engine.sync.retry-initial-delay-ms=5000
credential-engine.sync.retry-delay-multiplier=1.5
```

Map from env: `CREDENTIAL_ENGINE_SYNC_BATCH_SIZE`, etc. (optional in this phase; can wire in SyncService phase).

## Tests

- SyncRetryHelperTest: succeeds on first try; retries on failure then succeeds; exhausts retries and returns failure; verifies delay caps (mock or measure)
- SyncStatusJsonTest: serialize/deserialize; parse null returns default; parse invalid returns default

## Validate

```bash
sdk env
cd api && mvn test -Dtest=SyncRetryHelperTest,SyncStatusJsonTest
cd api && mvn test
```
