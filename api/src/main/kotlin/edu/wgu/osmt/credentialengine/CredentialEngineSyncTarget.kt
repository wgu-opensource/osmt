package edu.wgu.osmt.credentialengine

import com.fasterxml.jackson.databind.ObjectMapper
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.richskill.RichSkillDescriptor
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

/**
 * SyncTarget implementation that publishes to the Credential Engine Registry
 * via the Registry Assistant API.
 */
class CredentialEngineSyncTarget(
    private val registryUrl: String,
    private val apiKey: String,
    private val orgCtid: String,
    private val labelPrefix: String,
    private val canonicalUrlBase: String,
    private val appConfig: AppConfig,
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    private val ctidGenerator: CtidGenerator,
) : SyncTarget {
    private val logger = LoggerFactory.getLogger(CredentialEngineSyncTarget::class.java)
    private val baseUrl = registryUrl.trimEnd('/') + "/assistant"
    private val registryBase = registryUrl.trimEnd('/')

    override fun publishSkill(rsd: RichSkillDescriptor): Result<Unit> {
        val ctid = ctidGenerator.generate(rsd.uuid)
        logger.info("CE publish skill uuid={} ctid={}", rsd.uuid, ctid)
        val body =
            mapOf(
                "Competencies" to
                    listOf(
                        buildSkillMap(rsd, ctid, "Published"),
                    ),
                "PublishForOrganizationIdentifier" to orgCtid,
                "DefaultLanguage" to "en-US",
            )
        return post("$baseUrl/competency/publish", body).also { r ->
            if (r.isSuccess) {
                logger.info(
                    "CE publish skill success uuid={} {}",
                    rsd.uuid,
                    "$registryBase/finder/competency/$ctid",
                )
            }
        }
    }

    override fun deprecateSkill(rsd: RichSkillDescriptor): Result<Unit> {
        val ctid = ctidGenerator.generate(rsd.uuid)
        logger.info("CE deprecate skill uuid={} ctid={}", rsd.uuid, ctid)
        val body =
            mapOf(
                "Competencies" to
                    listOf(
                        buildSkillMap(rsd, ctid, "Deprecated"),
                    ),
                "PublishForOrganizationIdentifier" to orgCtid,
                "DefaultLanguage" to "en-US",
            )
        return post("$baseUrl/competency/publish", body).also { r ->
            if (r.isSuccess) {
                logger.info(
                    "CE deprecate skill success uuid={} {}",
                    rsd.uuid,
                    "$registryBase/finder/competency/$ctid",
                )
            }
        }
    }

    private fun buildSkillMap(
        rsd: RichSkillDescriptor,
        ctid: String,
        status: String,
    ): Map<String, Any> {
        val map =
            mutableMapOf<String, Any>(
                "CTID" to ctid,
                "CompetencyText" to (rsd.statement.ifBlank { rsd.name }),
                "CompetencyLabel" to applyPrefix(rsd.name).ifBlank { "Skill $ctid" },
                "Creator" to listOf(orgCtid),
                "ConceptKeyword" to
                    (rsd.searchingKeywords.mapNotNull { it.value }.take(20))
                        .map { it.replace("&", "and") }
                        .ifEmpty { listOf(rsd.name.take(100)) },
                "PublicationStatusType" to status,
            )
        // CE rejects Author and CompetencyCategory with "please provide a valid Competency publish request"
        if (status != "Deprecated") {
            val raw =
                canonicalUrlBase.ifBlank { appConfig.baseUrl }.trimEnd('/')
            val base =
                if (raw.contains("osmt.dev.")) {
                    "http://localhost:8080"
                } else {
                    raw
                }
            map["ExactAlignment"] = listOf(rsd.canonicalUrl(base))
        }
        return map
    }

    /**
     * CE requires SubjectWebpage or at least one member (HasMember, etc.);
     * "realistically at least two" members. SubjectWebpage = webpage that
     * describes the entity (CTDL); we always include the OSMT collection URL.
     */
    private fun buildCollectionMap(
        collection: Collection,
        ctid: String,
        skillCtids: List<String>,
        lifeCycleStatus: String,
    ): Map<String, Any> {
        val raw =
            canonicalUrlBase.ifBlank { appConfig.baseUrl }.trimEnd('/')
        val base =
            if (raw.contains("osmt.dev.")) "http://localhost:8080" else raw
        return mapOf(
            "CTID" to ctid,
            "Name" to applyPrefix(collection.name),
            "Description" to (collection.description ?: ""),
            "HasMember" to skillCtids,
            "SubjectWebpage" to collection.canonicalUrl(base),
            "OwnedBy" to listOf(mapOf("CTID" to orgCtid)),
            "LifeCycleStatusType" to lifeCycleStatus,
        )
    }

    override fun publishCollection(
        collection: Collection,
        skillCtids: List<String>,
    ): Result<Unit> {
        val ctid = ctidGenerator.generate(collection.uuid)
        val collMap = buildCollectionMap(collection, ctid, skillCtids, "Active")
        val body =
            mapOf(
                "Collection" to collMap,
                "PublishForOrganizationIdentifier" to orgCtid,
                "DefaultLanguage" to "en-US",
            )
        return post("$baseUrl/Collection/publish", body).also { r ->
            if (r.isSuccess) {
                logger.info(
                    "CE publish collection success uuid={} {}",
                    collection.uuid,
                    "$registryBase/finder/collection/$ctid",
                )
            }
        }
    }

    override fun deprecateCollection(collection: Collection): Result<Unit> {
        val ctid = ctidGenerator.generate(collection.uuid)
        val collMap = buildCollectionMap(collection, ctid, emptyList(), "Ceased")
        val body =
            mapOf(
                "Collection" to collMap,
                "PublishForOrganizationIdentifier" to orgCtid,
                "DefaultLanguage" to "en-US",
            )
        return post("$baseUrl/Collection/publish", body).also { r ->
            if (r.isSuccess) {
                logger.info(
                    "CE deprecate collection success uuid={} {}",
                    collection.uuid,
                    "$registryBase/finder/collection/$ctid",
                )
            }
        }
    }

    override fun unpublishAll(
        collectionUuids: List<String>,
        skillUuids: List<String>,
    ): Result<Unit> {
        for (uuid in collectionUuids) {
            val ctid = ctidGenerator.generate(uuid)
            delete("Collection", ctid).onFailure { return Result.failure(it) }
        }
        for (uuid in skillUuids) {
            val ctid = ctidGenerator.generate(uuid)
            delete("competency", ctid).onFailure { return Result.failure(it) }
        }
        return Result.success(Unit)
    }

    private fun delete(
        type: String,
        ctid: String,
    ): Result<Unit> {
        val url = "$baseUrl/$type/delete"
        val body =
            mapOf(
                "CTID" to ctid,
                "PublishForOrganizationIdentifier" to orgCtid,
            )
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("Authorization", "ApiToken $apiKey")
            }
        val entity = HttpEntity(objectMapper.writeValueAsString(body), headers)
        return try {
            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    String::class.java,
                )
            checkResponseBody(url, response.body, null)
        } catch (e: HttpStatusCodeException) {
            logger.warn(
                "CE delete failed: {} {}",
                e.statusCode,
                e.responseBodyAsString,
            )
            Result.failure(
                Exception(
                    "CE delete failed: ${e.statusCode} - " +
                        e.responseBodyAsString.take(200),
                ),
            )
        } catch (e: Exception) {
            logger.warn("CE delete error", e)
            Result.failure(e)
        }
    }

    private fun post(
        url: String,
        body: Map<String, Any?>,
    ): Result<Unit> {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("Authorization", "ApiToken $apiKey")
            }
        val json = objectMapper.writeValueAsString(body)
        logger.debug("CE publish request: {} body={}", url, json.take(2000))
        val entity = HttpEntity(json, headers)
        return try {
            val response =
                restTemplate.postForEntity(url, entity, String::class.java)
            checkResponseBody(url, response.body, json)
        } catch (e: HttpStatusCodeException) {
            logger.warn(
                "CE publish failed: {} {}",
                e.statusCode,
                e.responseBodyAsString,
            )
            Result.failure(
                Exception(
                    "CE publish failed: ${e.statusCode} - " +
                        e.responseBodyAsString.take(200),
                ),
            )
        } catch (e: Exception) {
            logger.warn("CE publish error", e)
            Result.failure(e)
        }
    }

    /**
     * CE returns HTTP 200 even on logical failures. Check response body
     * for Successful: false. See bin/test-credential-engine-sync.sh.
     */
    private fun checkResponseBody(
        url: String,
        responseBody: String?,
        requestBody: String?,
    ): Result<Unit> {
        if (responseBody.isNullOrBlank()) return Result.success(Unit)
        return try {
            val tree = objectMapper.readTree(responseBody)
            val first =
                when {
                    tree.isArray && tree.size() > 0 -> tree[0]
                    tree.isObject -> tree
                    else -> null
                }
            val successful = first?.get("Successful")?.asBoolean() ?: true
            val messagesNode = first?.get("Messages")
            val messages =
                when {
                    messagesNode != null && messagesNode.isArray -> {
                        (0 until messagesNode.size())
                            .map { messagesNode.get(it).asText() }
                    }

                    else -> {
                        emptyList()
                    }
                }
            val message =
                first?.get("Message")?.asText()
                    ?: messages.joinToString("; ")
            if (!successful) {
                logger.warn(
                    "CE publish failed (Successful=false): {} message={}",
                    url,
                    message,
                )
                requestBody?.let { body ->
                    val escaped = body.replace("'", "'\\''")
                    val curlCmd =
                        """
                        curl -sS -X POST "$url" \
                          -H "Content-Type: application/json" \
                          -H "Authorization: ApiToken ${'$'}CREDENTIAL_ENGINE_API_KEY" \
                          -d '$escaped'
                        """.trimIndent()
                    logger.warn("CE publish failed - copy to reproduce: {}", curlCmd)
                }
                Result.failure(
                    Exception(
                        "CE publish failed: Successful=false. $message",
                    ),
                )
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            logger.warn("CE response parse error: {}", e.message)
            Result.success(Unit)
        }
    }

    private fun applyPrefix(s: String): String = if (labelPrefix.isNotBlank()) "$labelPrefix $s" else s
}
