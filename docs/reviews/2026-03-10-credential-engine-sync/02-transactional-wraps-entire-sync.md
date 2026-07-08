# Class-Level `@Transactional` Wraps Entire Sync Loop

**Severity:** High
**File:** `SyncService.kt` (line 24), `SyncController.kt` (lines 161, 185)

## Problem

`SyncService` has class-level `@Transactional`:

```kotlin
@Service
@Transactional
class SyncService(...)
```

`SyncController` calls `syncService.syncAllSinceWatermark()` from a `ForkJoinPool.commonPool()` thread. Because `syncService` is a Spring proxy, the `@Transactional` interceptor fires — opening a database transaction that spans the **entire** `syncAllSinceWatermark()` call.

For a full resync of hundreds or thousands of records, this means:

1. **One DB connection held for the entire duration** (potentially minutes/hours). The connection pool has finite slots; holding one for a long sync can starve other requests.

2. **Watermark updates are not committed incrementally.** `syncStateRepository.updateWatermark()` joins the outer transaction (default propagation = `REQUIRED`). If the process crashes at record 500 of 1000, all 500 watermark advances are rolled back and the next sync restarts from 0.

3. **HTTP calls to Credential Engine happen inside the DB transaction.** Each `target.publishSkill()` is an HTTP round-trip (possibly with retries + `Thread.sleep`) while a DB connection is held open.

4. **Transaction timeout risk.** A full resync hitting CE rate limits could easily exceed default transaction timeout settings.

## Why It Matters

The entire purpose of incremental watermark advancement is crash recovery — "if we fail at record N, restart from N." With the outer `@Transactional`, the watermark isn't actually persisted until the whole job completes, defeating that purpose.

## Fix Options

**Option A (recommended): Remove class-level `@Transactional` from SyncService.**
Add `@Transactional` only to individual methods that genuinely need a single transaction (e.g., `syncRecord` for single-record sync). The batch methods should manage transactions explicitly at the per-record or per-batch level.

```kotlin
@Service
class SyncService(...) {
    @Transactional
    fun syncRecord(...): Result<Unit> = ...

    // NO @Transactional — manages its own transaction boundaries
    fun syncAllSinceWatermark(...): Result<Unit> = ...
}
```

**Option B: Use `Propagation.NOT_SUPPORTED` on `syncAllSinceWatermark`** to suspend any existing transaction. Then ensure `SyncStateRepository` methods use `Propagation.REQUIRES_NEW` so each watermark update commits independently.

Either way, each watermark advance should commit immediately so crash recovery works as intended.
