# Phase 4: CredentialEngineSyncTarget Delete + unpublishAll

## Scope of Phase

Implement CE delete API calls in `CredentialEngineSyncTarget`. Implement `unpublishAll()` to delete all collections then all skills. Note: orchestration (fetching which records to delete) happens in SyncService; this phase implements the target's `unpublishAll(uuidsToDelete)` or the target fetches internally.

**Design clarification**: The SyncTarget.unpublishAll() receives no args—the target must know what to delete. For CredentialEngineSyncTarget, we need the list of UUIDs (or CTIDs) to delete. Options:
- (A) SyncService fetches all Published/Archived UUIDs, passes them to `unpublishAll(collectionUuids, skillUuids)` — changes interface.
- (B) unpublishAll() accepts `(collectionUuids: List<String>, skillUuids: List<String>)` — interface becomes `fun unpublishAll(collectionUuids: List<String>, skillUuids: List<String>): Result<Unit>`.
- (C) SyncService fetches, then calls `deleteCollection(uuid)` and `deleteSkill(uuid)` in a loop — new interface methods.

Simplest: (B) Extend interface to `unpublishAll(collectionUuids: List<String>, skillUuids: List<String>): Result<Unit>`. SyncService fetches the UUIDs and passes them. MockSyncTarget ignores the params and just clears. CredentialEngineSyncTarget iterates and calls delete for each.

Update Phase 3: MockSyncTarget.unpublishAll(collectionUuids, skillUuids) — clear lists.

## Code Organization Reminders

- Add a private `delete` helper for the HTTP call; reuse for competency and Collection.
- CE delete uses HTTP DELETE with JSON body; RestTemplate.exchange with HttpMethod.DELETE.

## Implementation Details

### SyncTarget.kt (interface change)

```kotlin
fun unpublishAll(
    collectionUuids: List<String>,
    skillUuids: List<String>,
): Result<Unit>
```

### MockSyncTarget.kt

Update signature, clear lists (params ignored for mock).

### CredentialEngineSyncTarget.kt

Add private method:

```kotlin
private fun delete(type: String, ctid: String): Result<Unit> {
    val url = "$baseUrl/$type/delete"
    val body = mapOf(
        "CTID" to ctid,
        "PublishForOrganizationIdentifier" to orgCtid,
    )
    val headers = HttpHeaders().apply {
        contentType = MediaType.APPLICATION_JSON
        set("Authorization", "ApiToken $apiKey")
    }
    val entity = HttpEntity(objectMapper.writeValueAsString(body), headers)
    return try {
        restTemplate.exchange(
            url,
            HttpMethod.DELETE,
            entity,
            String::class.java,
        )
        Result.success(Unit)
    } catch (e: HttpStatusCodeException) {
        logger.warn("CE delete failed: {} {}", e.statusCode, e.responseBodyAsString)
        Result.failure(Exception("CE delete failed: ${e.statusCode} - ${e.responseBodyAsString.take(200)}"))
    } catch (e: Exception) {
        logger.warn("CE delete error", e)
        Result.failure(e)
    }
}
```

**CE API path**: Handbook shows `{type}/delete` e.g. `credential/delete`. Our publish paths are `competency/publish` and `Collection/publish`. So delete paths: `competency/delete`, `Collection/delete`. Use `competency` and `Collection` as the type parameter.

Implement:

```kotlin
override fun unpublishAll(
    collectionUuids: List<String>,
    skillUuids: List<String>,
): Result<Unit> {
    for (uuid in collectionUuids) {
        val ctid = ctidGenerator.generate(uuid)
        delete("Collection", ctid).onFailure { return Result.failure(it) }
    }
    for (uuid in skillUuids) {
        val ctid = ctidGenerator.generate(uuid)
        delete("competency", ctid).onFailure { return Result.failure(it) }
    }
    return Result.success(Unit)
}
```

**Order**: Collections first (so HasMember refs are removed), then skills. Single-threaded; consider adding a small delay between deletes if CE rate-limits (optional, can add later).

## Validate

```bash
sdk env && mvn -pl api compile
```

(No CE available in unit tests; manual verification when testing against sandbox.)
