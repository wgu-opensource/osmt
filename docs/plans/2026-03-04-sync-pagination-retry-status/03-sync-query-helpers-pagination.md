# Phase 3: SyncQueryHelpers Pagination

## Scope of Phase

- Add `limit` parameter to findSkillsUpdatedSince and findCollectionsUpdatedSince
- Cursor-based: `updateDate > watermark ORDER BY updateDate ASC LIMIT limit`
- Preserve existing semantics when limit is large (e.g. Int.MAX_VALUE for single-record sync if needed)

## Code Organization Reminders

- One concept per file
- Place more abstract things first

## Implementation Details

### 1. findSkillsUpdatedSince

In `api/src/main/kotlin/edu/wgu/osmt/credentialengine/SyncQueryHelpers.kt`:

Change signature:
```kotlin
fun findSkillsUpdatedSince(watermark: LocalDateTime?, limit: Int): List<RichSkillDescriptorDao>
```

- Add `.limit(limit)` to the select query
- Exposed: `slice(columns).select { ... }.orderBy(...).limit(limit)`
- Check Exposed API for limit – may be `limit(n)` on the query chain

### 2. findCollectionsUpdatedSince

Same pattern:
```kotlin
fun findCollectionsUpdatedSince(watermark: LocalDateTime?, limit: Int): List<CollectionDao>
```

### 3. Call Sites

- SyncService.processSkills / processCollections: pass batch size from config
- Any tests using these functions: pass a limit (e.g. 1000 for "fetch all" in tests)

### 4. Exposed Limit Syntax

Exposed uses `Slice.select(Columns, ...).where { }.orderBy().limit(n)`. Verify in Exposed docs:
- `Query.limit(limit, offset)` or similar

## Tests

- SyncQueryHelpersTest or in SyncServiceTest: verify limit is respected; with limit 2, returns at most 2 rows; ordering by updateDate preserved

## Validate

```bash
sdk env
cd api && mvn test
```
