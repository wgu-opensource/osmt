# Phase 2: Label Prefix in CredentialEngineSyncTarget

## Scope of Phase

Wire `labelPrefix` into `CredentialEngineSyncTarget` via `SyncTargetConfig`. Apply prefix to `CompetencyLabel` (skill name) and collection `Name` when non-blank.

## Code Organization Reminders

- Keep `CredentialEngineSyncTarget` focused; prefix logic is a simple string prepend.
- Place abstract/general logic first in files.

## Implementation Details

### SyncTargetConfig.kt

Inject `labelPrefix` when constructing `CredentialEngineSyncTarget`:

```kotlin
@Value("\${credential-engine.label-prefix:}") labelPrefix: String,
// ...
CredentialEngineSyncTarget(
    // ...existing params...
    labelPrefix = labelPrefix.trim(),
)
```

**Profile default**: Ensure `application-dev.properties` sets `credential-engine.label-prefix=(osmt-dev)` when `CREDENTIAL_ENGINE_LABEL_PREFIX` is unset (Phase 1). The `@Value` default of `:` means empty when the property is absent; dev profile overrides with the property file.

### CredentialEngineSyncTarget.kt

Add constructor parameter:

```kotlin
private val labelPrefix: String,
```

Add helper (private, at bottom):

```kotlin
private fun applyPrefix(s: String): String =
    if (labelPrefix.isNotBlank()) "$labelPrefix $s" else s
```

In `buildSkillMap()`, change:

```kotlin
"CompetencyLabel" to rsd.name,
```

to:

```kotlin
"CompetencyLabel" to applyPrefix(rsd.name),
```

In `publishCollection()` and `deprecateCollection()`, change the Collection map:

```kotlin
"Name" to collection.name,
```

to:

```kotlin
"Name" to applyPrefix(collection.name),
```

### Tests

- `CredentialEngineSyncTarget` is not directly unit-tested today; consider adding a test that verifies prefix is applied when set. If time-constrained, manual verification in Phase 8.

## Validate

```bash
sdk env && mvn -pl api test -Dtest=SyncServiceTest,SyncQueryHelpersTest -DfailIfNoTests=false
```
