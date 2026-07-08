# `ForkJoinPool.commonPool()` Is Wrong for Sync

**Severity:** Medium
**File:** `SyncController.kt` (lines 161, 185)

## Problem

Both `syncAll()` and `resyncAll()` launch the sync job on `ForkJoinPool.commonPool()`:

```kotlin
ForkJoinPool.commonPool().submit {
    try {
        syncService.syncAllSinceWatermark()
    } finally {
        syncInProgress.set(false)
    }
}
```

The common pool is shared by the entire JVM — parallel streams, `CompletableFuture.supplyAsync()`, and any other code that uses it. Its thread count defaults to `Runtime.getRuntime().availableProcessors() - 1`.

A sync job:
- Makes HTTP calls to Credential Engine (seconds per call)
- Retries with `Thread.sleep()` (up to 60s per retry)
- Runs potentially thousands of iterations

This blocks a common pool thread for the entire duration. On a typical 2–4 core deployment, that's 25–50% of the common pool capacity consumed.

## Impact

- Other code using the common pool (e.g., Kotlin coroutines, parallel streams) will be starved.
- If the pool is exhausted, sync itself could deadlock if any nested code also uses the common pool.
- `Thread.sleep()` in the retry helper is especially wasteful on a compute-oriented pool.

## Fix

Use a dedicated single-thread executor for sync jobs:

```kotlin
private val syncExecutor = Executors.newSingleThreadExecutor(
    ThreadFactory { r ->
        Thread(r, "ce-sync").apply { isDaemon = true }
    }
)
```

This also naturally serializes sync jobs (eliminating the need for `syncInProgress` AtomicBoolean) and makes the thread visible in thread dumps with a meaningful name.

For retry sleep specifically, consider using a `ScheduledExecutorService` or non-blocking delay, though this is less critical if sync runs on its own thread.
