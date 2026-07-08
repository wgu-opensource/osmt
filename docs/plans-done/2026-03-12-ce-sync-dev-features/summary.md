# CE Sync Dev Features — Plan Summary

## Completed

Two features for safe local/dev testing against the Credential Engine Registry:

1. **Label prefix** — `CREDENTIAL_ENGINE_LABEL_PREFIX`; dev profile defaults to `(osmt-dev)`. Applied to CompetencyLabel and collection Name only.

2. **Unpublish all** — `credential-engine.allow-unpublish-all`; dev profile defaults true. POST /api/sync/unpublish-all deletes all Published/Archived records from CE. Async, reuses sync state for progress.

## Implemented Phases

1. Config properties (label-prefix, allow-unpublish-all; dev defaults)
2. Label prefix in CredentialEngineSyncTarget
3. SyncTarget unpublishAll interface + MockSyncTarget
4. CredentialEngineSyncTarget CE delete + unpublishAll
5. SyncService unpublishAll orchestration
6. SyncController endpoint + allowUnpublishAll in state
7. UI Unpublish All button
8. Cleanup & validation

## File Changes

- `SyncTargetConfig`, `CredentialEngineSyncTarget`, `MockSyncTarget`, `SyncTarget`
- `SyncService`, `SyncController`, `SyncQueryHelpers`
- `RoutePaths`, `application.properties`, `application-dev.properties`
- `sync.service.ts`, `sync-management.component.ts/html`
- `CredentialEngineSyncTargetTest` (labelPrefix param)
- `docs/features/2026-03-03-credential-engine-sync.md`
