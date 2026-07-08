# Phase 3: SyncService – Core Sync Logic

## Scope of Phase

- SyncService: syncRecord(type, uuid), syncSinceWatermark(recordType)
- Fetch records with updateDate > watermark; skills before collections
- Update watermark on success; qualify Published/published (Archived → deprecate)

## Code Organization Reminders

- Place orchestration logic in SyncService
- Delegate to repositories and SyncTarget
- One concept per file; helpers at bottom

## Implementation Details

### 1. SyncService

Create `api/src/main/kotlin/edu/wgu/osmt/credentialengine/SyncService.kt`:

Inject: SyncTarget, SyncStateRepository, RichSkillRepository, CollectionRepository (or DAO access), AppConfig (baseUrl for canonical URLs).

**syncRecord(recordType: String, uuid: String): Result<Unit>**
- recordType = "skill" | "collection"
- Fetch by uuid from appropriate repo
- Check publish status: skip if Unpublished/draft
- Published → SyncTarget.publishSkill/publishCollection
- Archived → SyncTarget.deprecateSkill/deprecateCollection
- For collection: get member skill UUIDs, convert to CTIDs (ce-{uuid}), pass to publishCollection
- Return Result from SyncTarget

**syncSinceWatermark(syncKey: String, recordType: String): Result<Unit>**
- Get watermark from SyncStateRepository for (sync_type="credential-engine", syncKey, recordType)
- Query records: RichSkillDescriptor where updateDate > watermark AND (Published or Archived), ordered by updateDate asc
- Or Collection where updateDate > watermark AND (published or archived status)
- Process in batches if needed; for each success, track max updateDate
- After full success: updateWatermark with max updateDate
- On any failure: do NOT update watermark (transactional)
- Return Result.success or Result.failure

**syncAllSinceWatermark(syncKey: String): Result<Unit>**
- Call syncSinceWatermark("skill") first
- Then syncSinceWatermark("collection")
- Order matters: collections reference skill CTIDs

### 2. Record Type Constants

In SyncService or a shared object:
```kotlin
object SyncRecordType {
    const val SKILL = "skill"
    const val COLLECTION = "collection"
}
```

### 3. Sync Target Null Handling

When SyncTarget is null (prod, CE not configured), SyncService should handle gracefully: return Result.failure with message "Sync not configured".

### 4. Publish Status Mapping

- Skill: PublishStatus.Published → publish; PublishStatus.Archived → deprecate
- Collection: status published → publish; archived → deprecate (map per Collection.status enum)

## Tests

- `SyncServiceTest.kt`: with MockSyncTarget, verify syncRecord publishes; syncSinceWatermark updates watermark; syncAll runs skills before collections

## Validate

```bash
sdk env
cd api && mvn test -Dtest=SyncServiceTest
cd api && mvn test
```
