# Phase 3: SyncTarget unpublishAll Interface + MockSyncTarget

## Scope of Phase

Add `unpublishAll(): Result<Unit>` to `SyncTarget` interface. Implement in `MockSyncTarget` by clearing in-memory published/deprecated lists.

## Code Organization Reminders

- Interface first; implementations follow.
- Mock provides baseline behavior for tests.

## Implementation Details

### SyncTarget.kt

Add (see Phase 4 for signature with UUIDs):

```kotlin
/**
 * Deletes/unpublishes the given records from the target.
 * For CE: calls delete API per CTID. For mock: clears in-memory state.
 * Collections first, then skills. Only enabled when allow-unpublish-all.
 */
fun unpublishAll(
    collectionUuids: List<String>,
    skillUuids: List<String>,
): Result<Unit>
```

### MockSyncTarget.kt

Implement (params ignored; mock clears all):

```kotlin
override fun unpublishAll(
    collectionUuids: List<String>,
    skillUuids: List<String>,
): Result<Unit> {
    publishedSkills.clear()
    publishedCollections.clear()
    deprecatedSkills.clear()
    deprecatedCollections.clear()
    logger.info("MockSyncTarget: unpublishAll cleared all lists")
    return Result.success(Unit)
}
```

### SyncServiceTest.kt

Add test (calls mock directly; SyncService.unpublishAll comes in Phase 5):

```kotlin
@Test
fun `MockSyncTarget unpublishAll clears published state`() {
    val skill = randomSkill()
    syncService.syncSinceWatermark("default", SyncRecordType.SKILL)
    assertThat(mockSyncTarget.getPublishedSkillUuids()).contains(skill.uuid)

    mockSyncTarget.unpublishAll(emptyList(), emptyList())
    assertThat(mockSyncTarget.getPublishedSkillUuids()).isEmpty()
}
```

## Validate

```bash
sdk env && mvn -pl api test -Dtest=SyncServiceTest -DfailIfNoTests=false
```
