# Time Granularity Mismatch

**Severity:** Medium
**Files:** `SyncQueryHelpers.kt`, `SyncService.kt`

## Problem

The DB stores `updateDate` as `DATETIME(6)` (microsecond precision). The
watermark is also `DATETIME(6)`. However, `java.time.LocalDateTime` carries
nanosecond precision internally, and `java.sql.Timestamp.valueOf(LocalDateTime)`
truncates to nanoseconds that JDBC then maps to microseconds.

The real risk is in the **Kotlin-side cursor filter** in `SyncService.kt`:

```kotlin
it.updateDate > watermarkDate ||
    (it.updateDate == watermarkDate && it.id.value > watermarkId)
```

`LocalDateTime` comparison uses `compareTo`, which compares down to nanosecond.
If the watermark was round-tripped through MySQL (microsecond precision) but
the DAO's `updateDate` was loaded with a different Exposed read path, a
sub-microsecond difference could cause `==` to be `false` when the timestamps
represent the same DB value. This would cause the record to either:

- Pass the filter when it shouldn't (`>`), leading to a duplicate sync, or
- Fail the filter when it should pass, causing a skip.

## Scenario

1. Batch N processes skill id=100 with `updateDate = 2026-03-05T12:00:00.123456`.
2. Watermark is saved as `2026-03-05T12:00:00.123456`.
3. Next batch fetches skill id=101 with the same `updateDate`.
4. The DAO's `updateDate` is loaded by `findById()` (Exposed DAO path) which
   may round differently than the raw JDBC `Timestamp` that was stored as the
   watermark.
5. The `==` comparison fails → record falls through to the `>` branch → also
   fails → record is filtered out.

## Mitigation Already Present

The raw JDBC query already uses `id >= nextId` (where `nextId = watermarkId + 1`)
so the DB-side filter is correct. The Kotlin-side filter is defense-in-depth
only. A false negative here causes a re-fetch in the next batch (not data loss).

## Recommendation

Truncate both sides of the comparison to microsecond precision before comparing,
or compare using `ChronoUnit.MICROS`:

```kotlin
fun LocalDateTime.truncateToMicros(): LocalDateTime =
    truncatedTo(java.time.temporal.ChronoUnit.MICROS)
```
