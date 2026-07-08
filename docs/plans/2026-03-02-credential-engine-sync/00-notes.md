# Credential Engine Sync – Planning Notes

## Scope of Work

- **Sync infrastructure**: New `SyncState` table with `sync_type`, `sync_key`, `record_type`, `sync_watermark` (max updateDate of last successfully synced record)
- **Sync interface**: (1) Sync a specific record by ID, (2) Sync all records updated since last watermark
- **Credential Engine integration**: Publish competencies and collections to CE Registry via Assistant API (plain JSON format)
- **Configuration**: Env vars → Spring properties for CE API key, org CTID, registry URL, etc.
- **UI**: Sync management page – list integrations, show last sync watermarks, “Sync now” for incremental sync
- **Mock sync target**: For local dev – prints/keeps sync data in memory, enabled by default in dev
- **Design constraint**: Minimize changes to OSMT core; no new columns on existing tables; feature isolated for single-customer use

## Current State of Codebase

### API (Kotlin/Spring)
- **DB**: Flyway migrations in `api/src/main/resources/db/migration/`; Exposed ORM for Kotlin tables
- **Tables**: `RichSkillDescriptor`, `Collection` have `uuid`, `updateDate`, `creationDate`; no `ctid`
- **Admin pattern**: `ElasticSearchAdminController` – POST endpoints, admin role check via `oAuthHelper.hasRole(appConfig.roleAdmin)`, fire-and-forget background via `ForkJoinPool.commonPool().submit()`
- **Config**: `application-dev.properties`, `application.properties`; `AppConfig` for app settings
- **Profiles**: `dev`, `apiserver`, `import`, `reindex`, `single-auth`, `oauth2`; `SPRING_PROFILES_ACTIVE` from env

### UI (Angular)
- Routes in `app-routing.module.ts`; auth guard; role-based access via `auth-roles.ts`
- Environment via `EnvironmentService` / `window.__env` or `environment.ts`
- No existing admin/settings section; ES admin is backend-only (no UI)

### Docker / Deployment
- `api/docker/bin/docker_entrypoint.sh` – reads env vars, passes to JVM
- `osmt-staging.env.example` – documents env vars for staging

### Plan Reference Docs (this folder)
- CTDL mapping, publish API examples, collection HasMemberAdd/HasMemberRemove
- Competencies published individually; collections reference competency CTIDs via HasMember

## Questions

### Q1. Mock sync target – when is it active?
**Context**: Mock should be enabled by default in dev. Options: (a) Profile-based – `dev` profile uses mock, (b) Config-based – when CE env vars absent use mock, (c) Explicit flag – `SYNC_TARGET=mock` vs `credential-engine`.

**Suggested**: (b) Config-based. When `CREDENTIAL_ENGINE_API_KEY` (and required CE vars) are absent, use mock. In dev profile, those are never set by default. Explicit env vars override for real CE testing.

**Answer**: Config-based. Use mock when CE env vars are absent. Real CE when vars present.

### Q2. Sync execution – synchronous or asynchronous?
**Context**: ES admin runs sync in background (`ForkJoinPool.commonPool().submit()`), returns 202 Accepted. Large CE sync could be many HTTP calls.

**Suggested**: Async for “sync all since watermark”. Sync for “sync single record” (fast). Matches ES pattern, avoids timeouts.
**Answer**: Use existing ForkJoinPool.commonPool().submit() – async for bulk sync, sync for single record. No new async infrastructure.

### Q3. UI – where does sync management live?
**Context**: No admin/settings UI today. ES admin has no UI. Sync management needs to be discoverable.

**Suggested**: New top-level route `/admin/sync` or `/sync`, admin-only. Add nav link in header for admin users (similar to how other admin links might appear). Or under a generic “Admin” section if we add one.
**Answer**: Route `/admin/sync`, admin-only. Add nav link for admin users.

### Q4. Which records qualify for sync?
**Context**: Skills have publish status (Published, Archived, Unpublished). Collections have status (published, draft, archived, etc.). Plan says map Published→publish, Archived→deprecated.

**Suggested**: Sync only records that should appear in CE: Published skills (as competencies), published collections. Skip draft/unpublished.

**Answer**: Sync only Published skills and published collections. Skip draft/unpublished. (Archived: per plan, sync as deprecated when status changes.)
### Q5. Auth for sync endpoints and UI
**Context**: ES admin uses `appConfig.roleAdmin`. Same role likely appropriate.

**Suggested**: Reuse `roleAdmin` for sync API and sync management page. No new role.

**Answer**: Reuse roleAdmin for sync API and sync management page.
### Q6. Feature visibility when CE not configured
**Context**: In prod without CE env vars, should sync UI/API exist?

**Suggested**: When CE config absent AND mock disabled (e.g., prod), hide sync nav and return 503 or 404 from sync API. When mock enabled (dev), show sync UI and allow testing. Keeps prod clean when feature unused.

**Answer**: Show sync UI as disabled when CE not configured and mock not used. Indicate "Sync not configured" or similar. In dev (CE absent): use mock for testing. In prod (CE absent): disabled, no mock. Could implement NOP sync target if easier, but disabling is preferred.

### Q7. Record types for v1
**Context**: Plan mentions competencies (skills) and collections. Any others?

**Suggested**: `skill` and `collection` only for v1. Table design supports adding types later.

**Answer**: skill and collection only for v1.
