# Credential Engine Sync – Code Review

**Date:** 2026-03-05
**Scope:** `api/src/main/kotlin/edu/wgu/osmt/credentialengine/`

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
| `SyncState.kt` / `SyncStatusJson.kt` | Data models |

## Issues Found

| # | File | Severity | Summary |
|---|------|----------|---------|
| 1 | [time-granularity-mismatch.md](time-granularity-mismatch.md) | Medium | `LocalDateTime` equality in cursor filter may fail across precision boundaries |
| 2 | [count-vs-find-query-divergence.md](count-vs-find-query-divergence.md) | Medium | Count and find queries use different WHERE logic; pending count can be wrong |
| 3 | [draft-and-deleted-records-silently-skipped.md](draft-and-deleted-records-silently-skipped.md) | Low | Draft/Deleted records match the cursor query but are silently no-oped at sync time |
| 4 | [findbyid-npe-on-deleted-row.md](findbyid-npe-on-deleted-row.md) | High | `findById(...)!!` in raw JDBC path crashes if a row is deleted between query and lookup |
| 5 | [concurrency-and-instance-scoping.md](concurrency-and-instance-scoping.md) | Medium | `syncInProgress` is per-instance; multiple pods can run concurrent syncs |
| 6 | [collection-publishes-stale-skill-list.md](collection-publishes-stale-skill-list.md) | Low | Collection sync reads skill list at sync time, not at the time the collection was modified |
| 7 | [retry-blocks-common-pool-thread.md](retry-blocks-common-pool-thread.md) | Medium | `Thread.sleep` in retry helper blocks the common ForkJoinPool thread |
| 8 | [partial-batch-failure-loses-progress.md](partial-batch-failure-loses-progress.md) | Medium | A failure mid-batch loses progress for all successfully-synced records in that batch |

## Architecture Notes

The sync uses composite cursor pagination `(updateDate, lastRecordId)` to walk
records in deterministic order. A known Exposed/JDBC binding bug required a raw
JDBC workaround for the cursor branch (documented in
`docs/known-issues/2026-03-04-exposed-sync-cursor-infinite-loop.md`).

Defense-in-depth measures (Kotlin-side cursor filter, circuit breaker,
deduplication) are solid and well-tested.
