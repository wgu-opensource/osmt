# Phase 4: SyncController and API Routes

## Scope of Phase

- SyncController: GET /api/sync/state, POST sync single, POST sync all (async)
- Admin auth via roleAdmin
- RoutePaths update

## Code Organization Reminders

- Controller thin; delegate to SyncService
- Follow ElasticSearchAdminController pattern
- Admin auth on all endpoints

## Implementation Details

### 1. RoutePaths

Update `api/src/main/kotlin/edu/wgu/osmt/RoutePaths.kt`:

```kotlin
private const val SYNC_PATH = "/sync"
const val SYNC_STATE = "$SYNC_PATH/state"
const val SYNC_SKILL = "$SYNC_PATH/skill"
const val SYNC_SKILL_UUID = "$SYNC_SKILL/{uuid}"
const val SYNC_COLLECTION = "$SYNC_PATH/collection"
const val SYNC_COLLECTION_UUID = "$SYNC_COLLECTION/{uuid}"
const val SYNC_ALL = "$SYNC_PATH/all"
```

### 2. SyncController

Create `api/src/main/kotlin/edu/wgu/osmt/credentialengine/SyncController.kt`:

- Inject: SyncService, SyncStateRepository, AppConfig, OAuthHelper
- All endpoints: check `oAuthHelper.hasRole(appConfig.roleAdmin)`; 401 if not
- If SyncTarget is null (sync disabled): return 503 Service Unavailable with message

**GET /api/sync/state**
- Return list of sync states (e.g., for sync_key="default": skill and collection rows)
- Response: `{ "integrations": [ { "syncKey", "recordType", "syncWatermark" } ] }`
- Use SyncStateRepository to fetch

**POST /api/sync/skill/{uuid}**
- Sync single skill
- Call SyncService.syncRecord("skill", uuid)
- Return 200 on success; 400/404/500 on failure

**POST /api/sync/collection/{uuid}**
- Sync single collection
- Call SyncService.syncRecord("collection", uuid)

**POST /api/sync/all**
- Start bulk sync in background: `ForkJoinPool.commonPool().submit { syncService.syncAllSinceWatermark("default") }`
- Return 202 Accepted with message "Sync started. Check logs for progress."
- Match ES admin pattern

### 3. Response Types

Use simple ResponseEntity with body. For GET state, return JSON (Spring will serialize). For POST, return 200/202 with optional message body.

## Tests

- `SyncControllerTest.kt`: mock dependencies; verify 401 when not admin; verify 202 for sync all; verify 503 when sync disabled

## Validate

```bash
sdk env
cd api && mvn test -Dtest=SyncControllerTest
cd api && mvn test
```
