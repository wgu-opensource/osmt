# Phase 6: SyncController Unpublish-All Endpoint

## Scope of Phase

Add `POST /api/sync/unpublish-all` endpoint. Guard by `allowUnpublishAll`. Reuse `syncInProgress` so unpublish blocks sync and vice versa. Extend `GET /api/sync/state` response with `allowUnpublishAll: boolean`.

## Code Organization Reminders

- Follow existing controller patterns for sync endpoints.
- 202 response for async operations.

## Implementation Details

### RoutePaths.kt

Add:

```kotlin
const val SYNC_UNPUBLISH_ALL = "$SYNC_PATH/unpublish-all"
```

### SyncController.kt

Add to constructor or via SyncService: need `syncService.isUnpublishAllAllowed()`. SyncService already has it from Phase 5.

Extend `getSyncState()` response: The current `SyncStateResponse` has `integrations`. We need to add `allowUnpublishAll`. Create a wrapper or extend the response. Check how the frontend consumes it—SyncStateResponse is `{ integrations }`. We can add an optional `allowUnpublishAll?: boolean` to the response DTO. The controller builds `SyncStateResponse(integrations = ..., allowUnpublishAll = syncService.isUnpublishAllAllowed())`.

Add data class field:

```kotlin
data class SyncStateResponse(
    val integrations: List<SyncIntegrationDto>,
    val allowUnpublishAll: Boolean = false,
)
```

Update getSyncState to pass it.

Add endpoint:

```kotlin
@PostMapping(RoutePaths.SYNC_UNPUBLISH_ALL)
fun unpublishAll(): ResponseEntity<String> {
    ensureAdmin()
    ensureConfigured()
    if (!syncService.isUnpublishAllAllowed()) {
        throw ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Unpublish all is not enabled for this environment",
        )
    }
    if (!syncInProgress.compareAndSet(false, true)) {
        throw ResponseStatusException(
            HttpStatus.CONFLICT,
            "Sync or unpublish already in progress",
        )
    }
    val correlationId = syncService.markSyncInProgress("default")
    syncExecutor.submit {
        try {
            syncService.unpublishAll("default")
            // unpublishAll updates status (inProgress=false) on completion
        } finally {
            syncInProgress.set(false)
        }
    }
    return ResponseEntity("Unpublish started. Check logs for progress.", HttpStatus.ACCEPTED)
}
```

**Note**: SyncService.unpublishAll in Phase 5 should handle status updates internally (mark in progress at start, clear on completion), so the controller just needs to call it and the service does the rest. Re-check Phase 5: we said unpublishAll would call markSyncInProgress and update on completion. So the controller doesn't need to do markSyncInProgress—unpublishAll does it. But the controller currently does markSyncInProgress before submit for sync/resync. For consistency, we could have the controller do it for unpublish too, so the 202 returns immediately with "in progress" state. Let's have the controller do markSyncInProgress before submit, then in the runnable call unpublishAll. The unpublishAll would NOT call markSyncInProgress (to avoid double-set). It would only update status on completion. So:
- Controller: markSyncInProgress, submit { unpublishAll(); /* unpublishAll updates status when done */ }, return 202.
- SyncService.unpublishAll: fetch UUIDs, call target, on success/failure update status (inProgress=false).

### SyncService.unpublishAll status update

When complete, set inProgress=false for both integrations. Use syncStateRepository.updateStatusJson. Add a private helper or inline.

## Validate

```bash
sdk env && mvn -pl api test -Dtest=SyncServiceTest,SyncController* -DfailIfNoTests=false
```
