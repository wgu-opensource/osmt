# Phase 3: Add Env Var Merging to API

## Scope

Extend the API's `/whitelabel/whitelabel.json` endpoint to read `OSMT_*`
environment variables and merge them into the response. This is the core
mechanism that allows deployers to customize branding via Docker env vars.

## Code Organization Reminders

- Prefer a granular file structure, one concept per file.
- Place more abstract things, entry points, and tests **first**
- Place helper utility functions **at the bottom** of files.
- Keep related functionality grouped together
- Any temporary code should have a TODO comment so we can find it later.

## Implementation Details

### 1. Create WhitelabelConfig class

Create `api/src/main/kotlin/edu/wgu/osmt/config/WhitelabelConfig.kt`:

```kotlin
package edu.wgu.osmt.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Reads OSMT_* environment variables for whitelabel theming.
 *
 * Env var mapping:
 *   OSMT_TOOL_NAME        -> toolName
 *   OSMT_TOOL_NAME_LONG   -> toolNameLong
 *   OSMT_BRAND_COLOR      -> colorBrandAccent1
 *   OSMT_LOGO_URL         -> logoUrl
 *   OSMT_LICENSE_PRIMARY   -> licensePrimary
 *   OSMT_LICENSE_SECONDARY -> licenseSecondary
 *   OSMT_WHITELABEL_JSON  -> full JSON overlay
 */
@Component
class WhitelabelConfig(
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(
        WhitelabelConfig::class.java
    )

    private val envVarMapping = mapOf(
        "OSMT_TOOL_NAME" to "toolName",
        "OSMT_TOOL_NAME_LONG" to "toolNameLong",
        "OSMT_BRAND_COLOR" to "colorBrandAccent1",
        "OSMT_LOGO_URL" to "logoUrl",
        "OSMT_LICENSE_PRIMARY" to "licensePrimary",
        "OSMT_LICENSE_SECONDARY" to "licenseSecondary",
    )

    fun getJsonOverlay(): Map<String, Any> {
        val json = System.getenv("OSMT_WHITELABEL_JSON")
            ?: return emptyMap()
        return try {
            @Suppress("UNCHECKED_CAST")
            objectMapper.readValue(json, Map::class.java)
                as Map<String, Any>
        } catch (e: Exception) {
            log.warn(
                "Failed to parse OSMT_WHITELABEL_JSON: {}",
                e.message
            )
            emptyMap()
        }
    }

    fun getEnvVarOverrides(): Map<String, String> =
        envVarMapping.mapNotNull { (envVar, field) ->
            System.getenv(envVar)?.let { field to it }
        }.toMap()
}
```

### 2. Update UiController

In `api/src/main/kotlin/edu/wgu/osmt/ui/UiController.kt`, inject
`WhitelabelConfig` and update the merge order in `whitelabelConfig()`:

```kotlin
@Autowired
lateinit var whitelabelConfig: WhitelabelConfig

@GetMapping(
    "/whitelabel/whitelabel.json",
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
@ResponseBody
fun whitelabelConfig(): Map<String, Any> {
    // 1. Static whitelabel.json (defaults)
    val staticConfig = loadStaticConfig()

    // 2. OSMT_WHITELABEL_JSON overlay
    val jsonOverlay = whitelabelConfig.getJsonOverlay()

    // 3. Individual OSMT_* env vars
    val envOverrides = whitelabelConfig.getEnvVarOverrides()

    // 4. Dynamic auth config
    val dynamicConfig = buildDynamicAuthConfig()

    return staticConfig + jsonOverlay + envOverrides + dynamicConfig
}
```

Extract the existing static-config and dynamic-auth logic into private helper
methods (`loadStaticConfig()`, `buildDynamicAuthConfig()`) for readability.

### 3. Tests

Create `api/src/test/kotlin/edu/wgu/osmt/config/WhitelabelConfigTest.kt`:

```kotlin
@Test
fun `getEnvVarOverrides returns empty when no env vars set`() {
    // Default state — no OSMT_* vars
    val config = WhitelabelConfig(ObjectMapper())
    val overrides = config.getEnvVarOverrides()
    // Will be empty in test env unless env vars are set
    assertTrue(overrides.isEmpty() || overrides.isNotEmpty())
}

@Test
fun `getJsonOverlay returns empty when env var not set`() {
    val config = WhitelabelConfig(ObjectMapper())
    val overlay = config.getJsonOverlay()
    assertTrue(overlay.isEmpty())
}
```

Since `System.getenv()` is hard to mock, consider:
- Extracting env var reading into a function parameter / interface for
  testability
- Or testing the merge logic in `UiController` via integration test

Create or update `api/src/test/kotlin/edu/wgu/osmt/ui/UiControllerTest.kt`:

- Test that the endpoint returns default static config when no env vars are set
- Test that the merge order is correct (mock the WhitelabelConfig bean)

## Validate

```bash
cd api && sdk env && ./mvnw test
```
