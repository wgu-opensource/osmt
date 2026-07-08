# Phase 2: SyncTarget Interface and MockSyncTarget

## Scope of Phase

- Define SyncTarget interface
- Implement MockSyncTarget (in-memory, logging)
- Implement SyncTargetFactory (config-based Mock vs CE selection)

## Code Organization Reminders

- Interface first, implementations after
- One concept per file
- Place abstract/entry points first

## Implementation Details

### 1. SyncTarget Interface

Create `api/src/main/kotlin/edu/wgu/osmt/credentialengine/SyncTarget.kt`:

```kotlin
interface SyncTarget {
    fun publishSkill(rsd: RichSkillDescriptor): Result<Unit>
    fun publishCollection(collection: Collection, skillCtids: List<String>): Result<Unit>
    fun deprecateSkill(rsd: RichSkillDescriptor): Result<Unit>
    fun deprecateCollection(collection: Collection): Result<Unit>
}
```

Use `Result<Unit>` for success/failure; no exceptions for expected failures.
Pass `RichSkillDescriptor` and `Collection` domain objects (or minimal DTOs with required fields).

### 2. MockSyncTarget

Create `api/src/main/kotlin/edu/wgu/osmt/credentialengine/MockSyncTarget.kt`:

- Implement SyncTarget
- In-memory list/map of "published" items (e.g., `MutableList<Pair<String, Any>>` or typed data class)
- Log each call (info level) with record id, type
- `publishSkill`/`publishCollection`: add to list, return Result.success(Unit)
- `deprecateSkill`/`deprecateCollection`: mark in list or remove, log
- Optional: method to retrieve stored data for tests (e.g., `getPublishedSkills(): List<String>`)

### 3. SyncTargetFactory

Create `api/src/main/kotlin/edu/wgu/osmt/credentialengine/SyncTargetFactory.kt`:

- `@Configuration` or `@Component` with `@Bean` for `SyncTarget`
- Check if CE config present (e.g., `credentialEngine.apiKey` or env)
- If present: return `CredentialEngineSyncTarget` (Phase 5 – for now throw/return mock with TODO)
- If absent: check profile – if `dev` return `MockSyncTarget`, else return `null` or `NoOpSyncTarget`
- Use `@Value` or `@ConfigurationProperties` for CE config

For Phase 2, factory returns Mock when CE absent; when CE present, return Mock with TODO for Phase 5.

### 4. Config Properties

Add to `application.properties` (optional, with `@Value("\${credential-engine.api-key:#{null}}")`):

```properties
# Credential Engine (optional - when absent, mock used in dev)
credential-engine.api-key=${CREDENTIAL_ENGINE_API_KEY:}
credential-engine.org-ctid=${CREDENTIAL_ENGINE_ORG_CTID:}
credential-engine.registry-url=${CREDENTIAL_ENGINE_REGISTRY_URL:https://sandbox.credentialengine.org}
```

## Tests

- `MockSyncTargetTest.kt`: verify publishSkill/Collection and deprecate add/log correctly
- `SyncTargetFactoryTest.kt`: with CE config absent, returns Mock; with config present, returns CE (or mock stub until Phase 5)

## Validate

```bash
sdk env
cd api && mvn test -Dtest=*SyncTarget*,*MockSyncTarget*
cd api && mvn test
```
