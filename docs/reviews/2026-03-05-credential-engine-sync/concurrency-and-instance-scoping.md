# Concurrency and Instance Scoping

**Severity:** Medium
**File:** `SyncController.kt`

## Problem

The `syncInProgress` guard is an `AtomicBoolean` on the `SyncController`
instance. This prevents concurrent syncs **within a single JVM**, but has two
gaps:

### 1. Multiple application instances

If OSMT runs behind a load balancer with multiple pods/instances, each has its
own `SyncController` with its own `AtomicBoolean`. Two admins (or the same
admin hitting two different pods) can trigger concurrent syncs. Both will read
the same watermark and process the same records, causing:

- Duplicate CE publishes (idempotent for CE, but wasteful and confusing in logs)
- Race on watermark updates (last writer wins; could skip records or re-process
  them)

### 2. Controller restart resets the guard

If the application restarts while a background sync is running (on the
`ForkJoinPool`), `syncInProgress` resets to `false`. A new sync can be
triggered immediately, overlapping with any in-flight work from the old
instance's thread pool (if the JVM hasn't fully shut down yet).

### 3. ForkJoinPool.commonPool() is shared

The sync runs on `ForkJoinPool.commonPool()`, which is shared with all other
parallel stream / CompletableFuture work in the JVM. A long sync with many
retries (each sleeping up to 60s) can starve other parallel tasks.

## Recommendation

- Use a database-level lock (e.g., `SELECT ... FOR UPDATE` on the sync state
  row, or a dedicated lock table) to prevent cross-instance concurrent syncs.
- Use a dedicated `ExecutorService` instead of the common pool.
- On graceful shutdown, cancel or await the sync thread.
