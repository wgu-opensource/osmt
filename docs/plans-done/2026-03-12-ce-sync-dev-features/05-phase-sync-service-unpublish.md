# Phase 5: SyncService unpublishAll Orchestration

## Scope of Phase

Implement `SyncService.unpublishAll(syncKey)`: fetch all Published/Archived collections and skills, call `target.unpublishAll(collectionUuids, skillUuids)`. Run async on `ce-sync` executor. Add `isUnpublishAllAllowed()` for the controller. Reuse `syncInProgress` guard (unpublish blocks sync and vice versa).

## Code Organization Reminders

- Reuse `SyncQueryHelpers` or repository methods to fetch Published/Archived records.
- Keep orchestration in SyncService; target does the actual delete.

## Implementation Details

### Fetching UUIDs

Need all Published and Archived skills (with publishDate not null) and collections (status in Published, Archived). SyncQueryHelpers has `findSkillsUpdatedSince(null, null, limit)` and `findCollectionsUpdatedSince(null, null, limit)` for the first batch, but they paginate. For unpublish, we need all. Options:
- Add repository methods `findAllPublishedOrArchivedSkillUuids()` and `findAllPublishedOrArchivedCollectionUuids()` that return UUIDs.
- Or use existing queries with a large limit and loop until empty (simpler).

Simpler: Add top-level functions in SyncQueryHelpers or SyncService that collect all UUIDs. We already have the table/query logic; we can do a simple select of uuid where publishDate is not null / status in (Published, Archived). No pagination needed if we just need UUIDs—could be a single query.

```kotlin
// In SyncQueryHelpers or as SyncService helper
fun findAllPublishedOrArchivedSkillUuids(): List<String>
fun findAllPublishedOrArchivedCollectionUuids(): List<String>
```

Use Exposed: `RichSkillDescriptorTable.select { publishDate.isNotNull() }.map { it[RichSkillDescriptorTable.uuid] }` and similar for Collection. These run in a transaction—SyncService methods that call them should be @Transactional(readOnly = true) or we wrap in transaction { }.

Actually, unpublishAll will run on the background thread. The fetch of UUIDs can happen in a dedicated method that uses transaction { } or calls the repository. SyncStateRepository and similar use @Transactional. When we call from the background thread, we need to ensure DB access works. The earlier reverted fix added transaction { } for that reason—but it caused issues. For a simple select of UUIDs, we might be OK if the repository/DAO calls work. Let me assume we add a method to a repository or use SyncQueryHelpers with an explicit transaction block for the fetch. Keep it minimal.

Add to SyncQueryHelpers.kt:

```kotlin
fun findAllPublishedOrArchivedSkillUuids(): List<String> = transaction {
    RichSkillDescriptorTable
        .select { RichSkillDescriptorTable.publishDate.isNotNull() }
        .map { it[RichSkillDescriptorTable.uuid] }
}

fun findAllPublishedOrArchivedCollectionUuids(): List<String> = transaction {
    CollectionTable
        .select {
            CollectionTable.status inList
                listOf(PublishStatus.Published, PublishStatus.Archived)
        }
        .map { it[CollectionTable.uuid] }
}
```

### SyncService.kt

Add `isUnpublishAllAllowed(): Boolean` — read `@Value("\${credential-engine.allow-unpublish-all:false}") allowUnpublishAll: Boolean` (or use a dedicated config bean). Need profile-aware default: dev → true. Spring @Value with `:false` won't apply profile. Use a @ConfigurationProperties or compute in a config bean. Simpler: inject Environment and check:
- If activeProfiles contains "dev" and credential-engine.allow-unpublish-all is not explicitly set to false, then true.
- Else use credential-engine.allow-unpublish-all.

Actually the property `credential-engine.allow-unpublish-all` in application-dev.properties has default true. So when dev profile is active, the resolved value will be true (from the dev properties file). When not dev, application.properties has default false. So @Value("\${credential-engine.allow-unpublish-all:false}") will work—in dev, application-dev.properties sets it to true (or ${ENV:true}), so we get true. Good.

Add to SyncService:

```kotlin
@Value("\${credential-engine.allow-unpublish-all:false}")
private val allowUnpublishAll: Boolean,

fun isUnpublishAllAllowed(): Boolean = allowUnpublishAll

fun unpublishAll(syncKey: String = SYNC_KEY_DEFAULT): Result<Unit> {
    if (!allowUnpublishAll) {
        return Result.failure(IllegalStateException("Unpublish all is not enabled"))
    }
    val colUuids = findAllPublishedOrArchivedCollectionUuids()
    val skillUuids = findAllPublishedOrArchivedSkillUuids()
    return syncTargetOpt
        .map { it.unpublishAll(colUuids, skillUuids) }
        .orElse(Result.failure(IllegalStateException("Sync not configured")))
}
```

**Async execution**: The controller will submit unpublishAll to the executor, like syncAll. The guard: we need to ensure only one of (sync, resync, unpublish) runs at a time. The existing `syncInProgress` AtomicBoolean guards sync and resync. We can reuse it for unpublish—when unpublish runs, sync can't start and vice versa. In SyncController, we'll check syncInProgress before starting unpublish, and set it in the finally of the submitted runnable.

**Status updates**: Unpublish could update statusJson similar to sync—mark in progress, then done. The design said "reuse sync state for progress if straightforward." The current sync state has two integrations (skill, collection). For unpublish, we're doing one logical operation that touches both. We could set inProgress on both, then clear when done. Or add a separate "unpublish" status. Simplest: reuse. Mark both integrations inProgress, run unpublish, mark both not in progress. That way the existing UI "In progress" state will show. We need markSyncInProgress-style call and a completion update. Let's add a simpler path: just set inProgress on both at start, clear on finish (success or failure). Reuse markSyncInProgress for the start; add a "markSyncCompleted" or "markSyncAborted" that sets inProgress=false. We already have logic in syncBatchLoop that does updateStatusJson at the end. For unpublish, we'd do:
1. markSyncInProgress(syncKey) — sets both in progress
2. run unpublishAll
3. on success: updateStatusJson(inProgress=false, batchesCompleted=0 or similar)
4. on failure: updateStatusJson with error

So SyncService.unpublishAll would need to call syncStateRepository (or markSyncInProgress is in SyncService). The controller currently does markSyncInProgress before submitting. For unpublish, we'd do the same: mark in progress, submit, in the runnable we call unpublishAll, then on completion we need to clear in progress. The problem: unpublishAll is in SyncService, but the "clear in progress" logic would need to run after unpublishAll returns. So the flow in the controller runnable:

```kotlin
syncExecutor.submit {
    try {
        val result = syncService.unpublishAll("default")
        result.onFailure { syncService.markSyncAborted(...) }  // we reverted that
        result.onSuccess { syncService.markUnpublishComplete(...) }  // new
    } finally {
        syncInProgress.set(false)
    }
}
```

We need a method to set inProgress=false. Looking at SyncService, we have markSyncInProgress. We don't have markSyncComplete—syncBatchLoop does it internally. For unpublish, SyncService.unpublishAll doesn't run in a batch loop; it's a single call. So we need SyncService to update status when unpublish completes. Add a private method markUnpublishComplete(syncKey) that sets inProgress=false for both. And markUnpublishAborted(syncKey, error) for failure. SyncService.unpublishAll could take a callback, or we could have the controller call a separate method after. Simpler: SyncService.unpublishAll does the status update itself. So:

```kotlin
fun unpublishAll(syncKey: String): Result<Unit> {
    if (!allowUnpublishAll) return Result.failure(...)
    markSyncInProgress(syncKey)  // or a variant
    val colUuids = ...
    val skillUuids = ...
    val result = syncTargetOpt.map { it.unpublishAll(colUuids, skillUuids) }.orElse(...)
    result.fold(
        onSuccess = {
            syncStateRepository.resetWatermark(SYNC_TYPE, syncKey, SyncRecordType.SKILL)
            syncStateRepository.resetWatermark(SYNC_TYPE, syncKey, SyncRecordType.COLLECTION)
            /* update status inProgress=false for both */
        },
        onFailure = { /* update status with error */ }
    )
    return result
}
```

But markSyncInProgress is called by the controller before submitting. For unpublish, the controller would submit and the runnable would call syncService.unpublishAll(). So unpublishAll runs inside the executor. It could call markSyncInProgress at the start—but that's transactional and might interact with the fact we're on a background thread. Actually markSyncInProgress is @Transactional, so it should work when called from the service. And the controller won't call it—the runnable will call unpublishAll, which will do the status update. So in unpublishAll:
1. markSyncInProgress(syncKey) — so state shows in progress
2. fetch UUIDs, call target
3. update status on completion
4. on success: reset watermarks (so next sync republishes from scratch)

We have markSyncInProgress. For the completion update, we can use syncStateRepository.updateStatusJson for both record types with inProgress=false. Add a small helper or inline it.

### SyncController

Phase 6 will add the endpoint. Phase 5 focuses on SyncService.

### Tests

Add to SyncServiceTest:

```kotlin
@Test
fun `unpublishAll clears mock target when allowed`() {
    // Test config has allowUnpublishAll - need to ensure test config sets it
    val skill = randomSkill()
    syncService.syncSinceWatermark("default", SyncRecordType.SKILL)
    assertThat(mockSyncTarget.getPublishedSkillUuids()).contains(skill.uuid)

    val result = syncService.unpublishAll("default")
    assertThat(result.isSuccess).isTrue()
    assertThat(mockSyncTarget.getPublishedSkillUuids()).isEmpty()
}
```

The test uses MockSyncTarget. We need allowUnpublishAll=true in the test. SyncServiceTest has SyncTestConfig that provides MockSyncTarget. We need to also ensure allowUnpublishAll is true. The test properties or SyncTestConfig could set it. Add @TestPropertySource(properties = ["credential-engine.allow-unpublish-all=true"]) to the test class, or add the property to the test config.

## Validate

```bash
sdk env && mvn -pl api test -Dtest=SyncServiceTest -DfailIfNoTests=false
```
