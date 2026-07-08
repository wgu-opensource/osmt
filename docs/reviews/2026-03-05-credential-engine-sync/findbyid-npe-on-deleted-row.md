# findById NPE on Deleted Row

**Severity:** High
**File:** `SyncQueryHelpers.kt`

## Problem

The raw JDBC cursor queries (`findSkillsUpdatedSinceRaw`,
`findCollectionsUpdatedSinceRaw`) first fetch a list of IDs, then load each
entity via `findById`:

```kotlin
val ids = transaction { /* raw JDBC → list of Long */ }
return ids.map {
    RichSkillDescriptorDao.findById(EntityID(it, RichSkillDescriptorTable))!!
}
```

The `!!` (non-null assertion) will throw `NullPointerException` if a row is
deleted between the ID query and the `findById` call. This crashes the entire
sync job.

## Scenario

1. Raw JDBC query returns `[100, 101, 102]`.
2. Between the query and the `map` call, an admin deletes skill 101
   (or a cascade delete removes it).
3. `RichSkillDescriptorDao.findById(101)` returns `null`.
4. `!!` throws `NullPointerException`.
5. The sync job aborts with an unhandled exception. The watermark is not
   advanced for any of the three records.

## Impact

High. This is an unrecoverable crash for the sync job. The NPE is not caught
by the retry helper (which only retries the CE publish call, not the batch
fetch). The watermark stays where it was, so the next sync attempt will hit
the same deleted row and crash again — a permanent stuck state.

## Recommendation

Replace `!!` with `filterNotNull()` and log a warning for missing IDs:

```kotlin
val ids = transaction { /* ... */ }
return ids.mapNotNull { id ->
    RichSkillDescriptorDao.findById(EntityID(id, table))
        .also { if (it == null) log.warn("Row id={} vanished", id) }
}
```

This applies to both `findSkillsUpdatedSinceRaw` and
`findCollectionsUpdatedSinceRaw`.
