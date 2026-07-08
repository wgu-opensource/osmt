# Phase 2: Whitelabel Extensions for Split Deployment

## Scope of Phase

Extend the whitelabel system to support split deployment configuration. This includes:

1. Update `WhitelabelConfig.kt` to read new environment variables
2. Update `UiController.kt` to include split deployment fields in whitelabel JSON
3. Update `api/docker/whitelabel/whitelabel.json` with example values

## Code Organization Reminders

- Prefer a granular file structure, one concept per file
- Place more abstract things, entry points, and tests **first**
- Place helper utility functions **at the bottom** of files
- Keep related functionality grouped together
- Any temporary code should have a TODO comment so we can find it later

## Implementation Details

### 1. Update `WhitelabelConfig.kt`

**File**: `api/src/main/kotlin/edu/wgu/osmt/config/WhitelabelConfig.kt`

Add new environment variable mappings for split deployment:

```kotlin
package edu.wgu.osmt.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Reads OSMT_* environment variables and OSMT_WHITELABEL_JSON for whitelabel
 * theming. See docs/features/2026-03-19-whitelabel-theming.md.
 */
@Component
class WhitelabelConfig(
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(WhitelabelConfig::class.java)

    fun jsonOverlayFromEnv(): Map<String, Any> {
        val raw = System.getenv("OSMT_WHITELABEL_JSON") ?: return emptyMap()
        return try {
            @Suppress("UNCHECKED_CAST")
            (objectMapper.readValue(raw, Map::class.java) as Map<String, Any>)
        } catch (e: Exception) {
            log.warn("Failed to parse OSMT_WHITELABEL_JSON: {}", e.message)
            emptyMap()
        }
    }

    fun envVarOverrides(): Map<String, String> =
        ENV_KEYS
            .mapNotNull { (envKey, jsonKey) ->
                System
                    .getenv(envKey)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { jsonKey to it }
            }.toMap()

    companion object {
        private val ENV_KEYS =
            listOf(
                "OSMT_TOOL_NAME" to "toolName",
                "OSMT_TOOL_NAME_LONG" to "toolNameLong",
                "OSMT_BRAND_COLOR" to "colorBrandAccent1",
                "OSMT_LOGO_URL" to "logoUrl",
                "OSMT_LICENSE_PRIMARY" to "licensePrimary",
                "OSMT_LICENSE_SECONDARY" to "licenseSecondary",
                // NEW: Split deployment env vars
                "OSMT_INSTANCE_TYPE" to "instanceType",
                "OSMT_WRITABLE_INSTANCE_URL" to "writableInstanceUrl",
                "OSMT_WRITABLE_INSTANCE_NAME" to "writableInstanceName",
                "OSMT_READ_ONLY_MESSAGE" to "readOnlyMessage",
            )
    }
}
```

### 2. Update `UiController.kt`

**File**: `api/src/main/kotlin/edu/wgu/osmt/ui/UiController.kt`

Update `buildDynamicAuthConfig()` to include split deployment fields:

```kotlin
private fun buildDynamicAuthConfig(): Map<String, Any> {
    val dynamicConfig = mutableMapOf<String, Any>()

    // Existing fields
    if (appConfig.loginUrl.isNotBlank()) {
        dynamicConfig["loginUrl"] = appConfig.loginUrl
    }
    dynamicConfig["authMode"] = appConfig.authMode
    dynamicConfig["singleAuthEnabled"] = appConfig.singleAuthEnabled

    // NEW: Split deployment fields
    dynamicConfig["instanceType"] = appConfig.instanceType
    if (appConfig.writableInstanceUrl.isNotBlank()) {
        dynamicConfig["writableInstanceUrl"] = appConfig.writableInstanceUrl
    }
    if (appConfig.writableInstanceName.isNotBlank()) {
        dynamicConfig["writableInstanceName"] = appConfig.writableInstanceName
    }

    val providers = authConfigProvider?.getOAuthProviders() ?: emptyList()
    dynamicConfig["authProviders"] =
        providers.map {
            mapOf(
                "id" to it.id,
                "name" to it.name,
                "authorizationUrl" to it.authorizationUrl,
            )
        }
    return dynamicConfig
}
```

### 3. Update `AppConfig.kt`

**File**: `api/src/main/kotlin/edu/wgu/osmt/config/AppConfig.kt`

Add new properties for split deployment:

```kotlin
// Existing properties...
@Value("\${app.readOnlyMode:false}")
val readOnlyMode: Boolean = false,

@Value("\${app.instanceType:writable}")
val instanceType: String,

@Value("\${app.writableInstanceUrl:}")
val writableInstanceUrl: String,

@Value("\${app.writableInstanceName:Author Portal}")
val writableInstanceName: String,
```

### 4. Update `api/docker/whitelabel/whitelabel.json`

**File**: `api/docker/whitelabel/whitelabel.json`

Add example split deployment fields (commented out by default):

```json
{
  "editableAuthor": true,
  "defaultAuthorValue": "",
  "toolName": "OSMT",
  "toolNameLong": "Open Skills Management Tool",
  "publicSkillTitle": "Rich Skill Descriptor",
  "publicCollectionTitle": "Rich Skill Descriptor Collection",
  "licensePrimary": "Copyright © OSMT Contributors",
  "licenseSecondary": "All rights reserved.",
  "poweredBy": "",
  "poweredByUrl": "",
  "poweredByLabel": "",
  "colorBrandAccent1": "#1e40af",
  "logoUrl": "/assets/images/logo-light.svg",
  "_splitDeploymentComment": "Uncomment below for split deployment configuration",
  "_instanceType": "read-only",
  "_writableInstanceUrl": "https://author.example.com",
  "_writableInstanceName": "Author Portal",
  "_readOnlyMessage": "This is the public skill browser. To edit content, visit the Author Portal."
}
```

### 5. Update Test

**File**: `api/src/test/kotlin/edu/wgu/osmt/ui/WhitelabelControllerTest.kt`

Add test for split deployment fields:

```kotlin
@Test
fun `whitelabel json returns split deployment fields when configured`() {
    // This test requires configuring the app with writableInstanceUrl
    // For now, document the expected behavior
}
```

## Validate

Run the tests:

```bash
cd /Users/yona/dev/skybridge/osmt/api
mvn test -Dtest=WhitelabelControllerTest -q
```

Verify the backend compiles:

```bash
mvn compile -q
```

Test the whitelabel endpoint manually:

```bash
# Start the API with readonly profile
cd /Users/yona/dev/skybridge/osmt
OSMT_INSTANCE_TYPE=read-only OSMT_WRITABLE_INSTANCE_URL=https://author.example.com ./osmt_cli.sh -s

# In another terminal
curl http://localhost:8080/whitelabel/whitelabel.json | jq
```
