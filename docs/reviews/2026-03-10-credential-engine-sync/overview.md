# Credential Engine Sync – Pre-Merge Review

**Date:** 2026-03-10
**Branch:** `feature/sync`
**Scope:** Full sync stack — backend service, query helpers, controller, UI component, migrations
**Prior review:** `docs/reviews/2026-03-05-credential-engine-sync/`

## Files Reviewed

| File | Role |
|------|------|
| `SyncService.kt` | Orchestration, watermark advancement, batch loop |
| `SyncQueryHelpers.kt` | Cursor pagination queries (Exposed + raw JDBC) |
| `SyncStateRepository.kt` | Watermark and status CRUD |
| `SyncStateTable.kt` | Exposed schema for `SyncState` |
| `SyncController.kt` | REST endpoints, concurrency guard |
| `CredentialEngineSyncTarget.kt` | CE Registry Assistant HTTP client |
| `SyncRetryHelper.kt` | Exponential backoff retry wrapper |
| `SyncTargetConfig.kt` | Bean wiring, mock/real target selection |
| `CtidGenerator.kt` | UUIDv5 CTID generation |
| `SyncState.kt` / `SyncStatusJson.kt` | Data models |
| `CorrelationId.kt` | Random alphanumeric correlation ID |
| `sync-management.component.ts/html/scss` | Angular admin UI |
| `sync.service.ts` | Angular HTTP service |
| `V2026.03.02–09` migrations | Schema evolution |
| All `*Test.kt` and `*.spec.ts` files | Test coverage |

## Issues Found

| # | Severity | Summary | Detail |
|---|----------|---------|--------|
| 1 | **Critical** | Per-record progress updates drop `inProgress=true`, making the UI think sync finished mid-batch | [01-progress-drops-in-progress-flag.md](01-progress-drops-in-progress-flag.md) |
| 2 | **High** | Class-level `@Transactional` on SyncService wraps entire sync loop in one transaction | [02-transactional-wraps-entire-sync.md](02-transactional-wraps-entire-sync.md) |
| 3 | **Medium** | `ForkJoinPool.commonPool()` is wrong pool for long-running I/O-bound sync | [03-forkjoinpool-common-pool.md](03-forkjoinpool-common-pool.md) |
| 4 | **Medium** | Sync endpoints not explicitly secured in Spring Security; no-roles mode exposes them | [04-sync-endpoints-security.md](04-sync-endpoints-security.md) |
| 5 | **Low** | `markSyncInProgress` correlationId is discarded and replaced immediately | [05-correlation-id-mismatch.md](05-correlation-id-mismatch.md) |
| 6 | **Low** | Extensive type-switch duplication in `doSyncSinceWatermark` | [06-batch-loop-duplication.md](06-batch-loop-duplication.md) |

## Status of Previous Review Issues (2026-03-05)

| Prior # | Summary | Status |
|---------|---------|--------|
| 1 | Time-granularity mismatch | **Fixed** — composite cursor (date, id) eliminates precision issues |
| 2 | Count vs find query divergence | **Fixed** — raw JDBC count uses same predicate as find |
| 3 | Draft/deleted records silently skipped | **Accepted** — by-design behavior, no action needed |
| 4 | `findById(...)!!` NPE on deleted row | **Fixed** — now uses `mapNotNull` with `findById` null check and log warning |
| 5 | `syncInProgress` per-instance | **Open** — still per-JVM. Acceptable for single-instance deployment |
| 6 | Collection publishes stale skill list | **Open** — still reads skills at sync time, not at modification time |
| 7 | `Thread.sleep` in retry blocks common pool | **Open** — folded into issue #3 in this review |
| 8 | Partial batch failure loses progress | **Improved** — statusJson now records the failing record; watermark isn't advanced so no data loss, but records before the failure are re-published on retry |

## What's Working Well

- **Composite cursor pagination** is correct and well-tested. The raw JDBC workaround for the Exposed binding bug is minimal, documented, and defensive.
- **Circuit breaker** at 3 consecutive stuck batches is a smart safety net.
- **Kotlin-side cursor filter + deduplication** provides defense in depth against query-layer bugs.
- **UUIDv5 CTID generation** is deterministic and scoped by org — idempotent publishes.
- **Correlation IDs throughout** make log-tracing feasible in production.
- **UI component** is well-structured with good separation of concerns, auto-refresh logic, and sensible error handling.
- **Test coverage** is solid: SyncServiceTest, SyncQueryHelpersTest, SyncStateRepositoryTest, CredentialEngineSyncTargetTest, and component spec all pass and cover the important paths.
- **Migration chain** is clean — incremental ALTER TABLE additions with a backfill migration.
