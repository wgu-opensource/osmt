# CE Sync Dev Features — Design

## Scope of Work

Two features for safe local/dev testing against the Credential Engine Registry:

1. **Label prefix** — Configurable prefix (e.g. `(osmt-dev)`) on CE labels so dev records are distinguishable.
2. **Unpublish all** — Delete everything pushed to CE for dev cleanup.

## File Structure

```
api/src/main/kotlin/edu/wgu/osmt/
├── credentialengine/
│   ├── CredentialEngineSyncTarget.kt    # UPDATE: labelPrefix, delete(), apply prefix
│   ├── MockSyncTarget.kt               # UPDATE: unpublishAll()
│   ├── SyncController.kt               # UPDATE: POST /api/sync/unpublish-all
│   ├── SyncService.kt                  # UPDATE: unpublishAll()
│   ├── SyncTarget.kt                   # UPDATE: unpublishAll(): Result<Unit>
│   └── SyncTargetConfig.kt             # UPDATE: inject labelPrefix, allowUnpublishAll
├── RoutePaths.kt                       # UPDATE: SYNC_UNPUBLISH_ALL
api/src/main/resources/config/
├── application.properties              # UPDATE: label-prefix, allow-unpublish-all
└── config/
    └── application-dev.properties      # NEW/UPDATE: dev defaults
ui/src/app/admin/sync/
├── sync-management.component.ts        # UPDATE: unpublishAll, unpublishing state
├── sync-management.component.html      # UPDATE: Unpublish All section
└── sync.service.ts                     # UPDATE: unpublishAll(), allowUnpublishAll in state
```

## Conceptual Architecture

```
                    SyncController
                          |
          +---------------+---------------+
          |               |               |
   POST /sync/all   POST /sync/resync   POST /sync/unpublish-all
          |               |               |  (guarded by allow-unpublish-all)
          v               v               v
                    SyncService
                          |
          +---------------+---------------+
          |                               |
   syncAllSinceWatermark          unpublishAll()
   (existing)                     (collections first, then skills)
          |                               |
          v                               v
               SyncTarget (interface)
                          |
          +---------------+---------------+
          |                               |
   CredentialEngineSyncTarget     MockSyncTarget
   - publish/deprecate           - unpublishAll: clear lists
   - unpublishAll: CE delete
   - labelPrefix on CompetencyLabel, Name
```

## Main Components

### Label prefix

- **Config**: `credential-engine.label-prefix` from `CREDENTIAL_ENGINE_LABEL_PREFIX`
- **Profile default**: `dev` → `(osmt-dev)` when unset; others → empty
- **Apply to**: `CompetencyLabel` (skill name), collection `Name` only
- **Where**: `CredentialEngineSyncTarget.buildSkillMap()`, `publishCollection()`, `deprecateCollection()`

### Unpublish all

- **Config**: `credential-engine.allow-unpublish-all` from `CREDENTIAL_ENGINE_ALLOW_UNPUBLISH_ALL`
- **Profile default**: `dev` → true; others → false
- **Endpoint**: `POST /api/sync/unpublish-all`, admin-only, returns 202
- **Guard**: 503 if `allow-unpublish-all` false; 409 if sync/unpublish already in progress
- **Flow**: Load all Published/Archived collections and skills → delete collections first → delete skills → CE `competency/delete`, `Collection/delete`
- **Execution**: Async on `ce-sync` executor; reuse `syncInProgress` guard; reuse sync state for progress

### CE delete API

- Endpoints: `{registryUrl}/assistant/competency/delete`, `{registryUrl}/assistant/Collection/delete`
- Method: HTTP DELETE with JSON body `{ CTID, PublishForOrganizationIdentifier }`
- Auth: `Authorization: ApiToken {apiKey}`

### State response extension

- Add `allowUnpublishAll: boolean` to `GET /api/sync/state` so UI can show/hide Unpublish All section.
