# CTID Hash Generator

## Design

### Scope

Replace `"ce-" + uuid` CTID generation with a hash-based approach using
UUIDv5(orgCtid, entityUuid). This ensures globally unique, deterministic CTIDs
scoped to a CE organization, preventing collisions across OSMT deployments.

### File Structure

```
api/src/main/kotlin/edu/wgu/osmt/credentialengine/
├── CtidGenerator.kt              # NEW: UUIDv5 hash-based CTID generation
├── CredentialEngineSyncTarget.kt # UPDATE: use CtidGenerator, remove CTID_PREFIX
├── SyncService.kt                # UPDATE: use CtidGenerator, remove CTID_PREFIX/skillCtids
└── SyncTargetConfig.kt           # UPDATE: wire CtidGenerator bean

api/src/test/kotlin/edu/wgu/osmt/credentialengine/
├── CtidGeneratorTest.kt              # NEW: unit tests
└── CredentialEngineSyncTargetTest.kt # UPDATE: inject CtidGenerator

docs/features/
└── 2026-03-03-credential-engine-sync.md # UPDATE: document CTID scheme
```

### Architecture

```
credential-engine.org-ctid (config)
        │
        ▼
┌─────────────────┐
│  CtidGenerator   │  generate(entityUuid) → "ce-" + UUIDv5(orgCtid, entityUuid)
└────────┬────────┘
         │ injected into
    ┌────┴─────────────────┐
    │                      │
    ▼                      ▼
CredentialEngine       SyncService
  SyncTarget           .skillCtids()
  .publishSkill()
  .publishCollection()
```

### Main Components

| Component | Role |
|---|---|
| `CtidGenerator` | Holds orgCtid, derives namespace UUID, exposes `generate(entityUuid): String` |
| UUIDv5 impl | SHA-1 of (namespace bytes + name bytes), version=5, variant=RFC4122 |
| `SyncTargetConfig` | Creates `CtidGenerator` bean from `credential-engine.org-ctid` |
| `CredentialEngineSyncTarget` | Calls `ctidGenerator.generate(uuid)` instead of string concat |
| `SyncService.skillCtids()` | Same |

## Phases

## Phase 1: Implement CtidGenerator with UUIDv5

### Scope

Create `CtidGenerator.kt` with UUIDv5 logic and comprehensive unit tests.

### Code Organization Reminders

- Prefer a granular file structure, one concept per file.
- Place more abstract things, entry points, and tests first.
- Place helper utility functions at the bottom of files.
- Keep related functionality grouped together.
- Any temporary code should have a TODO comment.

### Implementation Details

#### 1. `CtidGenerator.kt`

```kotlin
package edu.wgu.osmt.credentialengine

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.UUID

class CtidGenerator(orgCtid: String) {
    private val namespaceUuid: UUID = uuidv5(OSMT_NAMESPACE, orgCtid)

    fun generate(entityUuid: String): String =
        "$CTID_PREFIX${uuidv5(namespaceUuid, entityUuid)}"

    companion object {
        private const val CTID_PREFIX = "ce-"
        private val OSMT_NAMESPACE: UUID =
            uuidv5(UUID(0, 0), "osmt.credentialengine.ctid")

        internal fun uuidv5(namespace: UUID, name: String): UUID {
            val sha1 = MessageDigest.getInstance("SHA-1")
            sha1.update(namespaceBytes(namespace))
            sha1.update(name.toByteArray(Charsets.UTF_8))
            val hash = sha1.digest()
            hash[6] = (hash[6].toInt() and 0x0F or 0x50).toByte() // version 5
            hash[8] = (hash[8].toInt() and 0x3F or 0x80).toByte() // variant RFC4122
            val buf = ByteBuffer.wrap(hash, 0, 16)
            return UUID(buf.long, buf.long)
        }

        private fun namespaceBytes(uuid: UUID): ByteArray {
            val buf = ByteBuffer.allocate(16)
            buf.putLong(uuid.mostSignificantBits)
            buf.putLong(uuid.leastSignificantBits)
            return buf.array()
        }
    }
}
```

Key design points:
- `OSMT_NAMESPACE` is a fixed UUIDv5 derived from the nil UUID + a constant
  string. This is our application-level namespace.
- `namespaceUuid` is derived from `OSMT_NAMESPACE` + `orgCtid`, so each org
  gets its own namespace.
- `generate()` produces `"ce-" + UUIDv5(namespaceUuid, entityUuid)`.
- `uuidv5` is `internal` so tests can verify RFC compliance directly.

#### 2. `CtidGeneratorTest.kt`

```kotlin
package edu.wgu.osmt.credentialengine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class CtidGeneratorTest {
    private val orgCtid = "ce-org-123"
    private val generator = CtidGenerator(orgCtid)

    @Test
    fun `generate returns ce- prefixed string`() {
        val ctid = generator.generate("abc-def-123")
        assertThat(ctid).startsWith("ce-")
    }

    @Test
    fun `generate is deterministic`() {
        val a = generator.generate("uuid-1")
        val b = generator.generate("uuid-1")
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `generate produces different CTIDs for different uuids`() {
        val a = generator.generate("uuid-1")
        val b = generator.generate("uuid-2")
        assertThat(a).isNotEqualTo(b)
    }

    @Test
    fun `different orgCtids produce different CTIDs for same uuid`() {
        val gen1 = CtidGenerator("ce-org-111")
        val gen2 = CtidGenerator("ce-org-222")
        val a = gen1.generate("same-uuid")
        val b = gen2.generate("same-uuid")
        assertThat(a).isNotEqualTo(b)
    }

    @Test
    fun `generate produces valid UUID format after ce- prefix`() {
        val ctid = generator.generate(UUID.randomUUID().toString())
        val uuidPart = ctid.removePrefix("ce-")
        val parsed = UUID.fromString(uuidPart)
        assertThat(parsed.version()).isEqualTo(5)
        assertThat(parsed.variant()).isEqualTo(2) // RFC4122
    }

    @Test
    fun `uuidv5 matches known test vector`() {
        // RFC 4122 Appendix B: name "python.org" with DNS namespace
        val dns = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8")
        val result = CtidGenerator.uuidv5(dns, "python.org")
        assertThat(result.toString())
            .isEqualTo("886313e1-3b8a-5372-9b90-0c9aee199e5d")
    }
}
```

### Validate

```bash
sdk env install && cd api && mvn test -pl . -Dtest="CtidGeneratorTest" -q
```

## Phase 2: Wire CtidGenerator into Sync Components

### Scope

Create the Spring bean, inject into `CredentialEngineSyncTarget` and
`SyncService`, remove duplicate `CTID_PREFIX` constants, update existing tests.

### Code Organization Reminders

- Prefer a granular file structure, one concept per file.
- Place more abstract things, entry points, and tests first.
- Place helper utility functions at the bottom of files.
- Keep related functionality grouped together.
- Any temporary code should have a TODO comment.

### Implementation Details

#### 1. `SyncTargetConfig.kt` — add `CtidGenerator` bean

```kotlin
@Bean
fun ctidGenerator(
    @Value("\${credential-engine.org-ctid:}") orgCtid: String,
): CtidGenerator? =
    if (orgCtid.isNotBlank()) CtidGenerator(orgCtid) else null
```

Pass to `CredentialEngineSyncTarget` constructor (add parameter). The bean is
null when sync is not configured, matching the existing `SyncTarget?` pattern.

#### 2. `CredentialEngineSyncTarget.kt`

- Add `ctidGenerator: CtidGenerator` constructor parameter.
- Remove `companion object { CTID_PREFIX }`.
- Replace all `"$CTID_PREFIX${rsd.uuid}"` with `ctidGenerator.generate(rsd.uuid)`.
- Replace all `"$CTID_PREFIX${collection.uuid}"` with
  `ctidGenerator.generate(collection.uuid)`.

#### 3. `SyncService.kt`

- Add `ctidGenerator: CtidGenerator?` constructor parameter (nullable, matching
  sync target optionality). Use `Optional<CtidGenerator>` to match the existing
  pattern if needed, or just take it from the sync target config.
- Remove `private const val CTID_PREFIX = "ce-"`.
- Update `skillCtids()`:
  ```kotlin
  private fun skillCtids(collectionDao: CollectionDao): List<String> =
      collectionDao.skills.map {
          ctidGenerator?.generate(it.uuid)
              ?: "ce-${it.uuid}"
      }
  ```
  The fallback shouldn't be reachable (sync only runs when configured), but
  keeps the code safe.

#### 4. `CredentialEngineSyncTargetTest.kt`

- Create a `CtidGenerator("ce-org-123")` in setUp.
- Pass to `CredentialEngineSyncTarget` constructor.
- Update CTID assertions: instead of `"ce-$skillUuid"`, compute the expected
  CTID via `ctidGenerator.generate(skillUuid)`.

#### 5. `SyncServiceTest.kt`

- The test uses `MockSyncTarget` which doesn't use CTIDs directly. Verify
  it still passes with no changes, or inject `CtidGenerator` if needed.

### Validate

```bash
sdk env install && cd api && mvn test -pl . \
  -Dtest="CtidGeneratorTest,CredentialEngineSyncTargetTest,SyncServiceTest" -q
```

## Phase 3: Cleanup and Validation

### Scope

Update feature docs, clean up stale code, run full validation.

### Code Organization Reminders

- Prefer a granular file structure, one concept per file.
- Place more abstract things, entry points, and tests first.
- Place helper utility functions at the bottom of files.
- Keep related functionality grouped together.
- Any temporary code should have a TODO comment.

### Implementation Details

#### 1. Update `docs/features/2026-03-03-credential-engine-sync.md`

Add a section documenting the CTID generation scheme:

```markdown
### CTID Generation

CTIDs are deterministically derived using UUIDv5 (RFC 4122, SHA-1):

    CTID = "ce-" + UUIDv5(namespaceUuid, entityUuid)

Where `namespaceUuid = UUIDv5(OSMT_NAMESPACE, orgCtid)`. This ensures:

- **Determinism:** Same record always produces the same CTID.
- **Deployment isolation:** Different `credential-engine.org-ctid` values
  produce different CTIDs, preventing collisions across instances.
- **Reverse correlation:** Skills include `ExactAlignment` with the OSMT URL
  containing the original UUID.
```

#### 2. Grep for stale references

```bash
git diff --cached | grep -E 'CTID_PREFIX|ce-\$'
```

Remove any leftover `CTID_PREFIX` constants or raw `"ce-"` concatenations
in sync code.

#### 3. Format check

```bash
cd ui && npm run format:check
```

### Validate

```bash
sdk env install && cd api && mvn test -q
```

### Plan Cleanup

Move plan to `docs/plans-done/`.

### Commit

```
feat(sync): hash-based CTID generation using UUIDv5

- Add CtidGenerator with UUIDv5(orgCtid, entityUuid) for deterministic,
  deployment-scoped CTIDs
- Wire as Spring bean, inject into CredentialEngineSyncTarget and SyncService
- Remove duplicate CTID_PREFIX constants
- Document CTID scheme in feature docs
```
