# Sync Duplicate Skill Diagnostic

If the same skill is published repeatedly during sync, run these checks.

**Note:** A raw JDBC workaround for the Exposed cursor bug is in place. See
`docs/known-issues/2026-03-04-exposed-sync-cursor-infinite-loop.md`.

## 1. Check for duplicate UUIDs in database

```sql
SELECT uuid, COUNT(*) as cnt
FROM RichSkillDescriptor
WHERE publishDate IS NOT NULL
GROUP BY uuid
HAVING cnt > 1;
```

If this returns rows, you have duplicate skills (schema violation - uuid has unique index). Fix by deduplicating the data.

## 2. Check SyncState watermark

```sql
SELECT sync_type, sync_key, record_type, sync_watermark, last_record_id
FROM SyncState
WHERE sync_type = 'credential-engine';
```

Verify `last_record_id` is populated after a sync. If it stays NULL, the composite cursor may not be advancing.

## 3. Log Exposed SQL to verify generated query

Add temporarily to see what Exposed generates:

```kotlin
// In DatabaseClient or a @PostConstruct, for dev only:
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.transactions.transaction
// In transaction block: transaction { addLogger(StdOutSqlLogger) }
```

Or enable `org.jetbrains.exposed.sql: DEBUG` if your logging config captures Exposed.

Expected SQL for composite cursor (watermarkDate=X, watermarkId=780):

```sql
WHERE ((updateDate > ?) OR ((updateDate = ?) AND (id > ?)))
```

If `id > 780` is missing or wrong, that's the Exposed EntityID comparison bug.

## 4. Run unit tests

```bash
cd api && mvn test -Dtest=SyncQueryHelpersTest,SyncServiceTest -Dtest='sync publishes each skill exactly once*'
```

- `SyncQueryHelpersTest`: Verifies findSkillsUpdatedSince returns no duplicates and cursor advances
- `SyncServiceTest.sync publishes each skill exactly once`: Verifies full sync publishes each skill exactly once

If tests pass but production duplicates persist, the issue is likely:
- Stale data (duplicate uuid rows)
- Different code version running
- Exposed entity cache returning same instance (see exposed/issues/653)
- Exposed EntityID `greater` comparison bug (id column with LongIdTable)

## Circuit breaker and workaround

The cursor query uses raw JDBC when `watermarkId != null` (see known-issues doc). SyncService
still applies a defensive Kotlin-side filter and circuit breaker: records with (updateDate, id) <=
watermark are excluded; if the same max id repeats for 3+ batches, sync aborts with a clear error.
