# Retry Blocks Common Pool Thread

**Severity:** Medium
**Files:** `SyncRetryHelper.kt`, `SyncController.kt`

## Problem

`SyncRetryHelper.withRetry()` uses `Thread.sleep()` for backoff delays. The
sync job runs on `ForkJoinPool.commonPool()` (submitted by `SyncController`).

With default settings (5 attempts, 5s initial delay, 1.5x multiplier), a
single failing record can block the common pool thread for:

```
5s + 7.5s + 11.25s + 16.875s = 40.625s
```

If multiple records fail, the total sleep time compounds. With the max cap
(10 attempts, 60s max delay), a single record can block for up to **~5
minutes**.

## Consequences

- `ForkJoinPool.commonPool()` is shared with all `parallelStream()`,
  `CompletableFuture.supplyAsync()`, and other default async work in the JVM.
  A sleeping sync thread reduces available parallelism for the entire
  application.
- The common pool's thread count defaults to `Runtime.availableProcessors() - 1`.
  On a small container (2 CPUs), one sleeping thread means 100% of the pool
  is blocked.

## Recommendation

1. **Use a dedicated executor** for sync work:
   ```kotlin
   private val syncExecutor = Executors.newSingleThreadExecutor(
       { r -> Thread(r, "ce-sync").apply { isDaemon = true } }
   )
   ```

2. **Consider non-blocking delay** using coroutines (`delay()`) or
   `ScheduledExecutorService` instead of `Thread.sleep()`.
