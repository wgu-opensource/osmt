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
                "OSMT_PUBLIC_INSTANCE_URL" to "publicInstanceUrl",
                "OSMT_AUTHORING_WELCOME_MESSAGE" to "authoringWelcomeMessage",
            )
    }
}
