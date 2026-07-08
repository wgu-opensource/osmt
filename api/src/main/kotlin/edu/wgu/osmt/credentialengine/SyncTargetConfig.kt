package edu.wgu.osmt.credentialengine

import com.fasterxml.jackson.databind.ObjectMapper
import edu.wgu.osmt.config.AppConfig
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.web.client.RestTemplate

@Configuration
class SyncTargetConfig {
    private val log = LoggerFactory.getLogger(SyncTargetConfig::class.java)

    @Bean
    fun credentialEngineRestTemplate(): RestTemplate = RestTemplate()

    @Bean
    fun ctidGenerator(
        @Value("\${credential-engine.org-ctid:}") orgCtid: String,
    ): CtidGenerator? = if (orgCtid.isNotBlank()) CtidGenerator(orgCtid) else null

    @Bean
    fun syncTarget(
        @Value("\${credential-engine.api-key:}") apiKey: String,
        @Value("\${credential-engine.org-ctid:}") orgCtid: String,
        @Value("\${credential-engine.registry-url:https://sandbox.credentialengine.org}")
        registryUrl: String,
        @Value("\${credential-engine.label-prefix:}") labelPrefix: String,
        @Value("\${credential-engine.canonical-url-base:}") canonicalUrlBase: String,
        appConfig: AppConfig,
        credentialEngineRestTemplate: RestTemplate,
        objectMapper: ObjectMapper,
        environment: Environment,
        ctidGenerator: CtidGenerator?,
    ): SyncTarget? {
        val profiles = environment.activeProfiles.toList()
        return when {
            apiKey.isNotBlank() && orgCtid.isNotBlank() -> {
                log.info("Sync target: Credential Engine (apiKey set)")
                CredentialEngineSyncTarget(
                    registryUrl = registryUrl,
                    apiKey = apiKey,
                    orgCtid = orgCtid,
                    labelPrefix = labelPrefix.trim(),
                    canonicalUrlBase = canonicalUrlBase.trim().trimEnd('/'),
                    appConfig = appConfig,
                    restTemplate = credentialEngineRestTemplate,
                    objectMapper = objectMapper,
                    ctidGenerator = ctidGenerator!!,
                )
            }

            profiles.any { it in listOf("dev", "single-auth") } -> {
                log.info("Sync target: MockSyncTarget (profiles={})", profiles)
                MockSyncTarget()
            }

            else -> {
                log.info("Sync target: not configured (profiles={})", profiles)
                null
            }
        }
    }
}
