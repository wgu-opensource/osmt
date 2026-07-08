# CE Sync Dev Features Plan

## Scope of Work

Two features to support safe local/dev testing against the real Credential Engine (CE) Registry:

1. **Label prefix for dev** — Add a configurable prefix (e.g. `(osmt-dev)`) to CE labels so dev records are easily distinguishable from production data.
2. **Unpublish all** — A way to delete/unpublish everything that was pushed to CE, so dev data can be cleaned up.

## Current State of the Codebase

### Sync Architecture

- **SyncTarget** interface: `publishSkill`, `publishCollection`, `deprecateSkill`, `deprecateCollection`
- **CredentialEngineSyncTarget**: POSTs to `{registryUrl}/assistant/competency/publish` and `.../Collection/publish`; no delete support
- **MockSyncTarget**: In-memory tracking, no HTTP
- **SyncTargetConfig**: Builds `CredentialEngineSyncTarget` when `api-key` + `org-ctid` set; `MockSyncTarget` when `dev` or `single-auth` profile
- **SyncController**: Admin-only endpoints under `/api/sync`; `sync/all`, `sync/resync`, `sync/skill/{uuid}`, `sync/collection/{uuid}`

### CE Payload Mapping (CredentialsEngineSyncTarget)

| OSMT Field      | CE Field         | Location                    |
|-----------------|------------------|-----------------------------|
| rsd.name        | CompetencyLabel  | buildSkillMap()             |
| rsd.statement   | CompetencyText   | buildSkillMap()             |
| collection.name | Name             | publishCollection body      |
| collection.description | Description | publishCollection body  |

### Config (application.properties)

- `credential-engine.api-key`, `credential-engine.org-ctid`, `credential-engine.registry-url`
- Sync tuning: `credential-engine.sync.batch-size`, retry settings
- No label prefix or unpublish-all settings

### CE Registry Assistant API (from handbook)

- **Delete**: `{registryUrl}/assistant/{CTDL object type}/delete` — HTTP DELETE with JSON body: `CTID`, `PublishForOrganizationIdentifier`, API key header
- Competency endpoint: `competency/publish` → likely `competency/delete`
- Collection endpoint: `Collection/publish` → likely `Collection/delete`
- CE policy: Prefer deprecation over delete; delete allowed for "bad data like duplicates"

### CTID Generation

- **CtidGenerator**: Deterministic `ce-` + UUIDv5(namespace, entityUuid); namespace derived from org-ctid
- CTIDs can be recomputed from UUIDs; no need to persist published CTIDs

---

## Questions to Resolve

### Q1: Label prefix — which fields to prefix?

**Context**: Skills have `CompetencyLabel` and `CompetencyText`; collections have `Name` and `Description`. Prefixing all could be noisy (e.g. long statements).

**Answer**: Prefix only `CompetencyLabel` and collection `Name`. Omit `CompetencyText` and `Description`.

---

### Q2: Label prefix — how to gate it?

**Context**: We don't want prod to accidentally get a prefix. Options: (a) env var only, (b) profile-based (e.g. dev/single-auth auto-add prefix), (c) require explicit `CREDENTIAL_ENGINE_LABEL_PREFIX` so empty = no prefix.

**Answer**: Env var `CREDENTIAL_ENGINE_LABEL_PREFIX`. `dev` profile defaults it to `(osmt-dev)` when not set; other profiles use empty (no prefix) unless explicitly set.

---

### Q3: Unpublish all — safety guard?

**Context**: Unpublish-all is destructive. Options: (a) separate `credential-engine.allow-unpublish-all=true`, (b) require label-prefix to be set (implies dev), (c) no guard, rely on admin auth.

**Answer**: Add `credential-engine.allow-unpublish-all`. `dev` profile defaults to true when unset; other profiles default false.

---

### Q4: Unpublish all — sync vs delete semantics?

**Context**: CE recommends deprecating over deleting. OSMT could either (a) call CE delete endpoint (removes from registry), (b) deprecate everything (keeps records but marks deprecated).

**Answer**: Use CE delete endpoint to actually remove data. Won't be enabled for production (gated by allow-unpublish-all). Document that CE policy prefers deprecation for normal lifecycle; delete is for test/dev cleanup.

---

### Q5: Unpublish all — execution mode?

**Context**: Unpublish could run many delete calls. Options: (a) synchronous (risks timeout), (b) async like sync-all (202 + background, poll state).

**Answer**: Async, same pattern as sync-all — 202 immediately, run on `ce-sync` executor, reuse `syncInProgress` guard. Reuse existing sync state for progress if straightforward.

---

## Notes

- Q1: Prefix CompetencyLabel and Name only.
- Q2: `CREDENTIAL_ENGINE_LABEL_PREFIX` env var; `dev` profile defaults to `(osmt-dev)` when unset; other profiles default empty.
- Q3: `credential-engine.allow-unpublish-all`; `dev` profile defaults true; other profiles default false.
- Q4: Use CE delete endpoint. Not enabled for prod; document CE prefers deprecation for normal lifecycle.
- Q5: Async, same pattern as sync-all. Reuse sync state for progress if straightforward.
