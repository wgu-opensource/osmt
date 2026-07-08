# Credential Engine Sync – Design

## Scope of Work

- Sync infrastructure: SyncState table (sync_type, sync_key, record_type, sync_watermark)
- Sync interface: sync single record by ID; sync all records updated since watermark
- Credential Engine integration: Publish competencies and collections via CE Assistant API
- Config: Env vars → Spring properties; mock when CE absent (dev), disabled when CE absent (prod)
- UI: /admin/sync – list integrations, watermarks, "Sync now" button
- Mock sync target: In-memory, logs data; enabled when CE vars absent in dev
- Constraint: Minimize core changes; no new columns on existing tables

## File Structure

```
api/src/main/
├── kotlin/edu/wgu/osmt/
│   ├── RoutePaths.kt                          # UPDATE: add sync paths
│   └── credentialengine/                      # NEW: isolated sync module
│       ├── SyncStateTable.kt                  # Exposed table
│       ├── SyncStateRepository.kt             # CRUD for SyncState
│       ├── SyncTarget.kt                      # Interface: publishSkill, publishCollection
│       ├── MockSyncTarget.kt                  # In-memory, logs sync data
│       ├── CredentialEngineSyncTarget.kt      # Real CE HTTP client
│       ├── SyncTargetFactory.kt               # Returns Mock or CE based on config
│       ├── SyncService.kt                     # Orchestrates sync, watermark updates
│       └── SyncController.kt                  # REST endpoints, admin auth
├── resources/
│   ├── config/
│   │   ├── application-dev.properties         # UPDATE: optional CE mock config
│   │   └── application.properties             # UPDATE: CE config properties (optional)
│   └── db/migration/
│       └── V2026.03.02__sync_state.sql        # NEW: SyncState table

ui/src/app/
├── app-routing.module.ts                      # UPDATE: add /admin/sync route
├── admin/
│   └── sync/
│       ├── sync-management.component.ts       # NEW
│       ├── sync-management.component.html     # NEW
│       └── sync-management.component.spec.ts  # NEW
└── navigation/
    └── header.component.html                 # UPDATE: Sync nav link (admin only)
```

## Conceptual Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                            SyncController                                │
│  GET /api/sync/state   POST /api/sync/skill/{uuid}   POST /api/sync/all │
│  (admin auth)          (sync single)                   (async, 202)     │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                            SyncService                                   │
│  • getState(syncKey, recordType) → watermark                             │
│  • syncRecord(type, uuid) → single publish                               │
│  • syncSinceWatermark(type) → batch, advance watermark on success        │
│  • Order: skills first, then collections                                 │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
              ▼                  ▼                  ▼
┌─────────────────┐  ┌──────────────────────┐  ┌──────────────────┐
│ SyncTarget      │  │ SyncStateRepository   │  │ RichSkillRepo,    │
│ (interface)     │  │ (watermark CRUD)      │  │ CollectionRepo    │
└────────┬────────┘  └──────────────────────┘  └──────────────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌─────────────┐  ┌─────────────────────────┐
│ MockSync    │  │ CredentialEngineSync    │
│ Target      │  │ Target                  │
│ (CE absent) │  │ (CE vars present)       │
└─────────────┘  └─────────────────────────┘
```

## Main Components

| Component | Responsibility |
|-----------|----------------|
| SyncStateTable | Exposed schema: sync_type, sync_key, record_type, sync_watermark |
| SyncStateRepository | Get/update watermark; ensure row exists per (sync_key, record_type) |
| SyncTarget | Interface: publishSkill(rsd), publishCollection(col, skillCtids), deprecateSkill, deprecateCollection |
| MockSyncTarget | Implement SyncTarget; store in memory, log; for dev when CE absent |
| CredentialEngineSyncTarget | Implement SyncTarget; HTTP to CE Assistant API |
| SyncTargetFactory | @Bean: if CE config present → CE, else → Mock (dev) or null (prod disabled) |
| SyncService | Fetch records since watermark, call SyncTarget, update watermark on success; skills before collections |
| SyncController | GET state, POST sync single, POST sync all (async); admin role check |

## Config / Environment

- `CREDENTIAL_ENGINE_API_KEY` – required for real CE
- `CREDENTIAL_ENGINE_ORG_CTID` – PublishForOrganizationIdentifier
- `CREDENTIAL_ENGINE_REGISTRY_URL` – sandbox vs prod
- When absent: dev → MockSyncTarget; prod → sync disabled in UI
