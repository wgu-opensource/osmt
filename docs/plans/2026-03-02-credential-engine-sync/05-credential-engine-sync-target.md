# Phase 5: CredentialEngineSyncTarget

## Scope of Phase

- Implement CredentialEngineSyncTarget
- CTDL mapping: OSMT → CE plain JSON
- HTTP client to CE Assistant API
- Publish competencies, collections; HasMemberAdd/Remove for collection updates

## Code Organization Reminders

- Isolate CE-specific mapping logic
- Use RestTemplate or WebClient; inject base URL, API key
- Reference plan docs for exact JSON shape

## Implementation Details

### 1. CredentialEngineSyncTarget

Create `api/src/main/kotlin/edu/wgu/osmt/credentialengine/CredentialEngineSyncTarget.kt`:

Inject: RestTemplate (or create bean), base URL, API key, org CTID.

**publishSkill(rsd: RichSkillDescriptor): Result<Unit>**
- Map RSD to CE Competency format per plan doc
- CTID = "ce-{rsd.uuid}"
- CompetencyText = statement; CompetencyLabel = name; Author = authors; CompetencyCategory = category; etc.
- OccupationType from job codes (SOC) – map to CE format
- POST to `{registryUrl}/assistant/competency/Publish`
- Headers: Authorization (API key), Content-Type application/json
- Return Result.success or Result.failure with error message

**publishCollection(collection: Collection, skillCtids: List<String>): Result<Unit>**
- Map to CE Collection format
- CTID = "ce-{collection.uuid}"
- HasMember = skillCtids
- POST to `{registryUrl}/assistant/Collection/publish`
- Or use HasMemberAdd if collection already exists (check plan for append vs full publish)

**deprecateSkill / deprecateCollection**
- CE may support PATCH or separate deprecation endpoint; check plan
- If not, may need to re-publish with PublicationStatusType=Deprecated

### 2. CTDL Mapping

Reference `docs/plans/2026-03-02-credential-engine-sync/` for field mapping.

Key mappings:
- skillName → competencyLabel
- skillStatement → competencyText
- author → Author
- category → competencyCategory
- keywords → conceptKeyword
- job codes → occupationType (CredentialAlignmentObject with SOC/ONET)
- exactAlignment: canonical OSMT URL (baseUrl + /api/skills/{uuid})

### 3. SyncTargetFactory Update

When CE config present, return CredentialEngineSyncTarget instead of Mock.

### 4. HTTP Client

Use Spring RestTemplate or WebClient. Handle 4xx/5xx; map to Result.failure. Log errors.

## Tests

- `CredentialEngineSyncTargetTest.kt`: use MockWebServer or WireMock to stub CE API; verify request body shape; verify success/failure handling

## Validate

```bash
sdk env
cd api && mvn test -Dtest=CredentialEngineSyncTargetTest
cd api && mvn test
```
