# Credential Engine Sync

OSMT can publish Rich Skill Descriptors (RSDs) and Collections to the Credential
Engine Registry via the Registry Assistant API. See also [Implementation Notes (WGU)](2026-03-03-credential-engine-sync-implementation-notes.md) for WGU use-case mapping and API references.

---

## Setup

### Required Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `CREDENTIAL_ENGINE_API_KEY` | Yes | _(empty — disables sync)_ | API key from Credential Engine |
| `CREDENTIAL_ENGINE_ORG_CTID` | Yes | _(empty — disables sync)_ | Your organization CTID (e.g. `ce-12345678-...`) |
| `CREDENTIAL_ENGINE_REGISTRY_URL` | No | `https://sandbox.credentialengine.org` | Registry base URL. Use `https://credentialengine.org` for production |

Both `CREDENTIAL_ENGINE_API_KEY` and `CREDENTIAL_ENGINE_ORG_CTID` must be
non-empty to enable live sync. When either is empty, sync is disabled and the
API returns `503 Service Unavailable`.

### Optional Tuning Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `CREDENTIAL_ENGINE_SYNC_BATCH_SIZE` | `20` | Records fetched per batch |
| `CREDENTIAL_ENGINE_SYNC_RETRY_ATTEMPTS` | `5` | Retries per record on failure (capped at 10) |
| `CREDENTIAL_ENGINE_SYNC_RETRY_INITIAL_DELAY_MS` | `5000` | First retry delay in ms |
| `CREDENTIAL_ENGINE_SYNC_RETRY_DELAY_MULTIPLIER` | `1.5` | Exponential backoff multiplier (delay capped at 60s) |
| `CREDENTIAL_ENGINE_LABEL_PREFIX` | _(empty)_ | Prefix for CE labels (e.g. `(osmt-dev)`). Dev profile defaults to `(osmt-dev)` when unset. |
| `CREDENTIAL_ENGINE_CANONICAL_URL_BASE` | _(empty)_ | Base URL for ExactAlignment links. When empty, uses `app.baseUrl`. Dev profile defaults to `https://example.org` (CE accepts resolvable or example.org links). |
| `CREDENTIAL_ENGINE_ALLOW_UNPUBLISH_ALL` | `false` | Enable Unpublish All. Dev profile defaults to `true` when unset. Not for production. |

OSMT omits Author and CompetencyCategory (CE rejects them) and replaces `&` with `and` in ConceptKeyword.

### Target Selection

| Condition | Sync target | Behavior |
|-----------|-------------|----------|
| API key + org CTID both set | `CredentialEngineSyncTarget` | Publishes to CE Registry via HTTP |
| Empty, `dev` or `single-auth` profile active | `MockSyncTarget` | Logs sync calls, no HTTP |
| Empty, other profiles | Disabled | 503 from all sync endpoints |

### Database Migrations

Flyway runs automatically on startup. The sync feature adds these migrations:

- `V2026.03.02__sync_state.sql` — creates `SyncState` table
- `V2026.03.04__sync_state_status_json.sql` — adds `status_json` column
- `V2026.03.05__sync_state_last_record_id.sql` — adds `last_record_id` column
- `V2026.03.06__sync_state_backfill_last_record_id.sql` — backfills existing rows
- `V2026.03.09__sync_cursor_indexes.sql` — composite indexes on
  `RichSkillDescriptor(updateDate, id)` and `Collection(updateDate, id)`

No manual migration steps are needed — deploy the new version and Flyway
handles it.

### Authentication

Sync endpoints require the admin role (`osmt.security.role.admin`, default
`ROLE_Osmt_Admin`). Enforced at two levels:

1. **Spring Security** — `/api/sync/**` requires admin authority (roles mode)
   or authentication (no-roles mode)
2. **Controller** — `ensureAdmin()` check against `appConfig.roleAdmin`

---

## API Endpoints

All endpoints are under `/api/sync`. All require admin authentication.

| Method | Path | Response | Description |
|--------|------|----------|-------------|
| GET | `/api/sync/state` | 200 JSON | Current sync state, watermarks, pending counts, allowUnpublishAll |
| POST | `/api/sync/all` | 202 text | Start incremental sync (async) |
| POST | `/api/sync/resync` | 202 text | Clear watermarks and resync everything (async) |
| POST | `/api/sync/unpublish-all` | 202 text | Delete all from CE (async). Requires allow-unpublish-all. |
| POST | `/api/sync/skill/{uuid}` | 200 | Sync a single skill (synchronous). Curator or Admin. |
| POST | `/api/sync/collection/{uuid}` | 200 | Sync a single collection (synchronous). Curator or Admin. |

### Error Responses

| Status | Meaning |
|--------|---------|
| 401 | Not authenticated or missing admin role |
| 404 | Skill/collection UUID not found (single-record endpoints) |
| 409 | Sync or unpublish already in progress |
| 503 | Sync not configured (CE env vars not set) or unpublish-all not enabled |

### `GET /api/sync/state` Response Shape

```json
{
  "integrations": [
    {
      "syncKey": "default",
      "recordType": "skill",
      "syncWatermark": "2026-03-10T14:30:00.064441",
      "pendingCount": 0,
      "statusJson": "{\"inProgress\":false,\"batchesCompleted\":3,\"sessionCorrelationId\":\"abc123xyz0\",\"lastUpdatedAt\":\"2026-03-10T14:30:05\"}"
    },
    {
      "syncKey": "default",
      "recordType": "collection",
      "syncWatermark": "2026-03-10T14:30:01.123456",
      "pendingCount": 2,
      "statusJson": null
    }
  ]
}
```

| Field | Type | Description |
|-------|------|-------------|
| `syncKey` | string | Integration key (currently always `"default"`) |
| `recordType` | string | `"skill"` or `"collection"` |
| `syncWatermark` | string or null | ISO timestamp of last successfully synced record; null if never synced |
| `pendingCount` | int or null | Records not yet synced; null while sync is in progress |
| `statusJson` | string or null | JSON string with job status (see below) |

**`statusJson` fields** (when parsed):

| Field | Type | Description |
|-------|------|-------------|
| `inProgress` | boolean | `true` while sync is running |
| `batchesCompleted` | int | Number of batches processed |
| `sessionCorrelationId` | string | Correlation ID for searching application logs |
| `lastUpdatedAt` | string | ISO timestamp of last status update |
| `error` | object or null | Error details if sync failed |
| `error.message` | string | Error description |
| `error.correlationId` | string | Error-specific correlation ID |
| `error.recordUuid` | string | UUID of the record that failed |
| `error.recordName` | string | Name of the record that failed |

---

## UI: Admin Sync Page

Navigate to `/admin/sync` (also linked from the admin nav as **Sync**).

The page shows:
- **Summary banner** — up-to-date, pending count, in progress, or error
- **Details table** — per-integration watermarks, pending counts, status, and
  correlation IDs (click to copy)

**Actions:**

- **Sync New Changes** — incremental sync of records modified since the last
  watermark.
- **Resync All** — clears all watermarks and re-publishes every published
  skill and collection from scratch. Use for recovery after errors or bulk
  changes. Can take a long time on large datasets.
- **Unpublish All** — deletes all published skills and collections from CE.
  Shown only when `allow-unpublish-all` is enabled (dev default). Use to clean
  up dev/test data. CE policy prefers deprecation for normal lifecycle; delete
  is for test/dev cleanup.

**Auto-refresh:** when sync is in progress, enable the auto-refresh checkbox
to poll every 5 seconds until completion (auto-stops after 1 hour).

### Individual Resync (Skill and Collection Pages)

On the manage page for a skill (`/skills/:uuid/manage`) or collection
(`/collections/:uuid/manage`), a **Sync to Credential Engine** button is
available to authors (Curator role) and admins. It triggers an immediate
single-record sync for that skill or collection, useful when CE data is out
of date (e.g. a collection shows no RSDs). Success triggers a toast and
reload; on 503, the toast indicates sync is not configured.

---

## Test Script

Verify CE connectivity before running a full sync:

```bash
./bin/test-credential-engine-sync.sh
```

Sources `api/osmt-dev-stack.env` and `api/osmt-staging.env` if present.
Requires `curl` and `jq`.

---

## Sync Behavior

- **Incremental sync** (`POST /api/sync/all`): processes skills first (so
  collection `HasMember` CTIDs are valid), then collections. Advances the
  watermark after each successfully published record.
- **Resync** (`POST /api/sync/resync`): clears watermarks for both record
  types, then runs a full incremental sync from the beginning.
- **Single-record sync** (`POST /api/sync/skill/{uuid}` or
  `.../collection/{uuid}`): publishes or deprecates one record synchronously.
  Does not update watermarks.
- **Async execution**: sync-all and resync run on a dedicated background thread
  (`ce-sync`). The API returns 202 immediately. Poll `GET /api/sync/state` to
  track progress.
- **Retry**: each record publish is retried with exponential backoff on failure.
  A batch aborts on the first record that exhausts all retries; the error
  is recorded in `statusJson`.
- **Watermarks**: composite cursor `(updateDate, lastRecordId)` for
  deterministic pagination. Incremental sync skips already-synced records.

---

## OSMT → Credential Engine Translation

### Skill (RSD) → Competency

| OSMT Field          | CE/CTDL Field           | Notes                           |
| ------------------- | ----------------------- | ------------------------------- |
| `uuid`              | `CTID`                  | Hash-based; see [CTID Generation](#ctid-generation) |
| `name`              | `CompetencyLabel`       | Skill name; optional prefix via `label-prefix` |
| `statement`         | `CompetencyText`        | Skill statement                 |
| (org CTID)          | `Creator`               | List with org CTID from config  |
| `authors`           | _(omitted)_             | CE rejects; see Known Limitations |
| `categories`        | _(omitted)_             | CE rejects; see Known Limitations |
| `searchingKeywords` | `ConceptKeyword`        | Keyword values, max 20; `&` → `and` |
| (status)            | `PublicationStatusType` | `"Published"` or `"Deprecated"` |
| canonical URL       | `ExactAlignment`        | `{baseUrl}/api/skills/{uuid}`   |

### Collection → Collection

| OSMT Field    | CE/CTDL Field         | Notes                          |
| ------------- | --------------------- | ------------------------------ |
| `uuid`        | `CTID`                | Hash-based; see [CTID Generation](#ctid-generation) |
| `name`        | `Name`                | Collection name; optional prefix via `label-prefix` |
| `description` | `Description`         | Collection description         |
| (derived)     | `HasMember`           | Skill CTIDs (hash-based)        |
| (derived)     | `SubjectWebpage`      | OSMT collection URL, single string (CE API expects string, not array) |
| (org CTID)    | `OwnedBy`             | List with org CTID from config |
| (status)      | `LifeCycleStatusType` | `"Active"` or `"Ceased"`       |

### CTID Generation

CTIDs are deterministically derived using UUIDv5 (RFC 4122, SHA-1):

    CTID = "ce-" + UUIDv5(namespaceUuid, entityUuid)

Where `namespaceUuid = UUIDv5(OSMT_NAMESPACE, credential-engine.org-ctid)`.
This ensures:

- **Determinism:** Same record always produces the same CTID.
- **Deployment isolation:** Different `credential-engine.org-ctid` values
  produce different CTIDs, preventing collisions across instances.
- **Reverse correlation:** Skills include `ExactAlignment` with the OSMT URL
  containing the original UUID.

### Status and Deprecation

- **Published skill** → `PublicationStatusType: "Published"`
- **Archived skill** → Re-publish with `PublicationStatusType: "Deprecated"`
- **Published collection** → `LifeCycleStatusType: "Active"`
- **Archived collection** → Re-publish with `LifeCycleStatusType: "Ceased"`

### CE Registry Assistant API

- **Handbook**: <https://credreg.net/registry/assistant>
- **Competencies**: <https://credreg.net/registry/competencies>
- Competency endpoint: `POST {registryUrl}/assistant/competency/publish`
- Collection endpoint: `POST {registryUrl}/assistant/Collection/publish`
- Auth header: `Authorization: ApiToken {apiKey}`

---

## Known Limitations

- **CE rejects Author, CompetencyCategory, and ampersand in ConceptKeyword:**
  The CE Registry Assistant API returns HTTP 200 with `Successful: false` and
  message "Error - please provide a valid Competency publish request." when the
  competency payload includes `Author`, `CompetencyCategory`, or an ampersand
  (`&`) in any `ConceptKeyword` value. OSMT therefore omits Author and
  CompetencyCategory entirely, and replaces `&` with `and` in keywords.
  Reproduce with: `./bin/test-credential-engine-sync.sh --full`.
- **CE requires SubjectWebpage or 2+ members for collections:** Collections with
  fewer than two `HasMember` entries are rejected without `SubjectWebpage`.
  OSMT always includes `SubjectWebpage` (OSMT collection URL) per CTDL: webpage
  that describes the entity.
- **Single-instance only:** The sync-in-progress guard (`AtomicBoolean`) is
  in-process. Running multiple API instances could start duplicate syncs.
  For multi-instance deployment, add a distributed lock (database row lock
  or Redis).
- **Stale skill list on collections:** Collection sync reads the current skill
  membership at sync time. If skills were added/removed between the
  collection's modification and its sync, the published `HasMember` list may
  differ from the state at modification time.
- **No job code mapping:** SOC/O*NET job codes are not yet mapped to CE
  `OccupationType` / `CredentialAlignmentObject`.
