package edu.wgu.osmt.ui

import com.fasterxml.jackson.databind.ObjectMapper
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.config.WhitelabelConfig
import edu.wgu.osmt.config.WhitelabelMerge
import edu.wgu.osmt.security.AuthConfigProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class UiController {
    @Autowired
    lateinit var appConfig: AppConfig

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired(required = false)
    var authConfigProvider: AuthConfigProvider? = null

    @Autowired
    lateinit var osmtWhitelabelConfig: WhitelabelConfig

    @RequestMapping()
    fun index(): String = javaClass.getResource("/ui/index.html")?.readText(Charsets.UTF_8) ?: "UI not configured"

    @GetMapping(
        "/whitelabel/whitelabel.json",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @ResponseBody
    fun whitelabelConfig(): Map<String, Any> {
        val staticConfig = loadStaticWhitelabel()
        val mergedBranding =
            WhitelabelMerge.mergeBrandingLayers(
                staticConfig,
                osmtWhitelabelConfig.jsonOverlayFromEnv(),
                osmtWhitelabelConfig.envVarOverrides(),
            )
        val dynamicConfig = buildDynamicAuthConfig()
        return mergedBranding + dynamicConfig
    }

    private fun loadStaticWhitelabel(): Map<String, Any> =
        try {
            val staticJson =
                javaClass
                    .getResource("/docker/whitelabel/whitelabel.json")
                    ?.readText(Charsets.UTF_8)
            if (staticJson != null) {
                @Suppress("UNCHECKED_CAST")
                (
                    objectMapper.readValue(staticJson, Map::class.java)
                        as Map<String, Any>
                )
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }

    private fun buildDynamicAuthConfig(): Map<String, Any> {
        val dynamicConfig = mutableMapOf<String, Any>()
        if (appConfig.loginUrl.isNotBlank()) {
            dynamicConfig["loginUrl"] = appConfig.loginUrl
        }
        dynamicConfig["authMode"] = appConfig.authMode
        dynamicConfig["singleAuthEnabled"] = appConfig.singleAuthEnabled
        dynamicConfig["readOnlyMode"] = appConfig.readOnlyMode
        if (appConfig.publicInstanceUrl.isNotBlank()) {
            dynamicConfig["publicInstanceUrl"] = appConfig.publicInstanceUrl
        }
        if (appConfig.authoringWelcomeMessage.isNotBlank()) {
            dynamicConfig["authoringWelcomeMessage"] = appConfig.authoringWelcomeMessage
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
}
