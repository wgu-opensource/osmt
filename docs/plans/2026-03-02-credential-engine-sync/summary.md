# Credential Engine Sync – Implementation Summary

## Overview

OSMT can publish Rich Skill Descriptors and Collections to the Credential Engine
Registry via the Registry Assistant API. Sync is admin-only and config-based;
when CE environment variables are absent, a mock target is used in dev or sync is
disabled in prod.

## Completed Work

### Phase 1: SyncState table and repository

- `SyncStateTable` Exposed schema for sync watermark per (syncKey, recordType)
- `SyncState` entity and `SyncStateRepository` (getOrCreateRow, getWatermark,
  getSyncState)
- Flyway migration `V44__sync_state_table.sql`

### Phase 2: SyncTarget interface and MockSyncTarget

- `SyncTarget` interface: publishSkill, publishCollection, deprecateSkill,
  deprecateCollection
- `MockSyncTarget` implementation for dev/testing
- `SyncTargetConfig` bean: returns MockSyncTarget in dev when CE vars absent;
  returns CredentialEngineSyncTarget when CE vars set; Optional.empty() otherwise

### Phase 3: SyncService core logic

- `SyncService`: syncRecord, syncSinceWatermark, syncAllSinceWatermark,
  getSyncState, isConfigured
- `SyncQueryHelpers`: findSkillsUpdatedSince, findCollectionsUpdatedSince
- Watermark-based incremental sync

### Phase 4: SyncController and API routes

- `SyncController` (admin auth required):
  - GET /api/sync/state → SyncStateResponse with integrations
  - POST /api/sync/skill/{uuid}
  - POST /api/sync/collection/{uuid}
  - POST /api/sync/all → 202, async background sync

### Phase 5: CredentialEngineSyncTarget

- `CredentialEngineSyncTarget`: RestTemplate-based HTTP client
- CTDL mapping: CompetencyLabel, CompetencyText, Creator, Author,
  CompetencyCategory, ConceptKeyword, ExactAlignment
- Competency: POST .../assistant/competency/publish
- Collection: POST .../assistant/Collection/publish
- PublicationStatusType / LifeCycleStatusType for deprecation
- `CredentialEngineSyncTargetTest` with mockk

### Phase 6: UI sync management page

- `/admin/sync` route, AuthGuard + SyncManage role (OSMT_ADMIN)
- `SyncManagementComponent`: integrations table, Sync Now button, 503 handling
- `SyncService` (UI): getState, syncAll
- Nav link "Sync" in header for admins
- `ButtonAction.SyncManage` in auth-roles

### Phase 7: Config and integration

- `application.properties`: credential-engine.* config
- `osmt-staging.env.example`: CREDENTIAL_ENGINE_* env vars
- `application-dev.properties`: commented log level for credentialengine
- `SyncStateTable` in ApiServer tableList (already present)

### Phase 8: Cleanup and validation

- No temporary code (TODO/FIXME/console.log) in sync files
- Format check passed; no linter errors

## Configuration

| Variable                    | Required | Default                                   |
| --------------------------- | -------- | ----------------------------------------- |
| CREDENTIAL_ENGINE_API_KEY   | Yes*     | (empty)                                   |
| CREDENTIAL_ENGINE_ORG_CTID  | Yes*     | (empty)                                   |
| CREDENTIAL_ENGINE_REGISTRY_URL | No   | https://sandbox.credentialengine.org      |

*When both API_KEY and ORG_CTID are set, CE sync is enabled. Otherwise: mock
in dev, disabled in prod.

## Files Created/Modified

- `api/`: CredentialEngineSyncTarget, SyncTargetConfig, SyncController,
  SyncService, SyncState*, SyncQueryHelpers, MockSyncTarget, SyncTarget,
  RoutePaths (sync), migration, tests
- `ui/`: admin/sync/*, auth-roles, header, app-routing, app.module
- `docs/`: plan phases, summary
