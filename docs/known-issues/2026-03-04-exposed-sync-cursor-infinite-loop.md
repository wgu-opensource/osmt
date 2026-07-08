# Exposed ORM Sync Cursor Infinite Loop

**Date:** 2026-03-04  
**Component:** Credential Engine sync (`SyncService`, `SyncQueryHelpers`)  
**Status:** Workaround applied 2026-03-05; root cause in Exposed/JDBC layer

## Summary

The Credential Engine sync uses composite cursor pagination `(watermarkDate, watermarkId)` to fetch published skills/collections in batches. An Exposed/JDBC binding bug caused the `id >= nextId` predicate to be ignored—MySQL returned records that should have been excluded (e.g. id=780 when cursor was `id >= 781`). This led to an infinite loop or circuit-breaker abort.

## Workaround Applied

**We bypass Exposed entirely for the composite-cursor branch** and use raw JDBC:

- **Location:** `SyncQueryHelpers.kt` – functions suffixed with `Raw`: `findSkillsUpdatedSinceRaw`, `findCollectionsUpdatedSinceRaw`, `countSkillsUpdatedSinceRaw`, `countCollectionsUpdatedSinceRaw`
- **Trigger:** When `watermarkDate != null && watermarkId != null` (i.e. resuming from a prior cursor)
- **Implementation:** Inside `transaction { }`, obtain `TransactionManager.current().connection.connection as java.sql.Connection`, then:
  - `conn.prepareStatement(sql)` with `?` placeholders
  - `ps.setTimestamp(1, ts)`, `ps.setTimestamp(2, ts)` for datetime
  - `ps.setLong(3, nextId)` for the id threshold
  - `ps.setInt(4, limit)` for LIMIT
  - Execute and map results to DAOs via `findById`

**Why this level?** Only direct `PreparedStatement.setLong()` binding produces correct results. Every Exposed path we tried failed.

## What We Tried (All Failed)

1. **Exposed DSL with EntityID:** `id greaterEq EntityID(watermarkId+1, Table)` — SQL looked correct (`id >= 781`) but MySQL returned 780.
2. **Exposed DSL with raw Long:** `id greaterEq nextId` using `ExpressionWithColumnType<EntityID<T>>.greaterEq(t: T)` — same wrong result.
3. **Exposed `exec()` with IColumnType args:** `exec(sql, listOf(LongColumnType() to nextId, ...))` — same wrong result.

Exposed’s parameter binding (DSL or exec) appears to bind the id value incorrectly for this query with MySQL, regardless of how we pass the Long. Raw JDBC `ps.setLong()` bypasses that layer and works.

## Justification

- **Scope is minimal:** Only the four cursor/count helpers when `watermarkId != null`. All other sync logic (non-cursor fetches, single-record lookups, updates) continues to use Exposed.
- **Query is stable:** The cursor SQL is simple and unlikely to need schema changes.
- **Defense in depth:** `SyncService` still retains the Kotlin-side cursor filter and circuit breaker for robustness.
- **Reversibility:** If Exposed fixes the bug, we can remove the raw path and revert to DSL.

## Symptoms (Pre-Workaround)

- Sync repeatedly processed the same skill (e.g. id=780) batch after batch.
- Logs: `watermark advanced to id=780` then `Batch N raw from DB: ids=[780]` again.
- Circuit breaker: `Sync cursor stuck at id=780 - Exposed query returns same record repeatedly`.

## References

- `api/src/main/kotlin/edu/wgu/osmt/credentialengine/SyncQueryHelpers.kt` – Raw JDBC cursor queries
- `api/src/main/kotlin/edu/wgu/osmt/credentialengine/SyncService.kt` – Filter and circuit breaker
- Exposed 0.30.2, MySQL Connector/J, `LongIdTable` / `EntityID`
