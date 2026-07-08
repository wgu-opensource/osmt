# Collection Publishes Stale Skill List

**Severity:** Low
**File:** `SyncService.kt`, `CredentialEngineSyncTarget.kt`

## Problem

When a collection is synced, its `HasMember` list (skill CTIDs) is read at
sync time from the DAO's current `skills` relation:

```kotlin
private fun skillCtids(collectionDao: CollectionDao): List<String> =
    collectionDao.skills.map { "$CTID_PREFIX${it.uuid}" }
```

This reads whatever skills are currently in the collection, not what was in it
when it was last modified (the `updateDate` that triggered the sync).

## Scenario

1. Collection C has skills [A, B]. `updateDate` = T1.
2. Admin adds skill D to collection C. `updateDate` = T2.
3. Sync runs and fetches C (because `updateDate > watermark`).
4. Between fetch and publish, admin removes skill B.
5. CE receives `HasMember: [A, D]` — skill B is silently dropped without
   being in the change that triggered the sync.

## Impact

Low. The collection will be re-synced when skill B's removal updates the
collection's `updateDate` (if it does). But if the collection's `updateDate`
is not bumped by skill removal, CE will be inconsistent until the next manual
resync.

## Recommendation

This is generally acceptable for eventually-consistent sync. Document that
CE reflects the current state of the collection at sync time, not a
point-in-time snapshot. If strict consistency is needed, consider
snapshotting the skill list at query time within the same transaction.
