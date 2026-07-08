package edu.wgu.osmt.credentialengine

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptor
import io.mockk.every
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.util.UUID

class CredentialEngineSyncTargetTest {
    private val registryUrl = "https://sandbox.credentialengine.org"
    private val apiKey = "test-api-key"
    private val orgCtid = "ce-org-123"
    private val appConfig =
        AppConfig(
            baseDomain = "localhost",
            baseUrl = "https://osmt.example.org",
            defaultAuthorName = "OSMT",
            defaultAuthorUri = "https://osmt.example.org",
            defaultCreatorUri = "https://osmt.example.org",
            frontendUrl = "https://osmt.example.org",
            loginUrl = "",
            authMode = "oauth2",
            singleAuthEnabled = false,
            oauth2RolesClaim = "roles",
            loginSuccessRedirectUrl = "/",
            userName = "name",
            userIdentifier = "email",
            allowPublicSearching = true,
            allowPublicLists = true,
            publicKeywordLimit = 1000,
            enableRoles = false,
            baseLineAuditLogIfEmpty = false,
            rsdContextUrl = "https://rsd.example.org",
            corsAllowedOrigins = "*",
            singleAuthAdminUsername = null,
            singleAuthAdminPassword = null,
            roleAdmin = "Osmt_Admin",
            roleCurator = "Osmt_Curator",
            roleView = "Osmt_View",
            scopeRead = "SCOPE_osmt.read",
            sessionTokenSecret = "",
            sessionTokenExpirySeconds = 86400,
            sessionTokenIssuer = "",
            readOnlyMode = false,
            publicInstanceUrl = "",
            authoringWelcomeMessage = "",
        )
    private val objectMapper =
        ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())

    private lateinit var restTemplate: RestTemplate
    private lateinit var target: CredentialEngineSyncTarget
    private val ctidGenerator = CtidGenerator(orgCtid)

    @BeforeEach
    fun setUp() {
        restTemplate = io.mockk.mockk(relaxed = false)
        target =
            CredentialEngineSyncTarget(
                registryUrl = registryUrl,
                apiKey = apiKey,
                orgCtid = orgCtid,
                labelPrefix = "",
                canonicalUrlBase = "",
                appConfig = appConfig,
                restTemplate = restTemplate,
                objectMapper = objectMapper,
                ctidGenerator = ctidGenerator,
            )
    }

    @Test
    fun `publishSkill sends competency with correct shape`() {
        val skillUuid = UUID.randomUUID().toString()
        val rsd =
            RichSkillDescriptor(
                id = 1L,
                creationDate = LocalDateTime.now(),
                updateDate = LocalDateTime.now(),
                uuid = skillUuid,
                name = "Test Skill",
                statement = "Can do X",
                keywords =
                    listOf(
                        Keyword(1L, LocalDateTime.now(), LocalDateTime.now(), KeywordTypeEnum.Author, "Jane"),
                        Keyword(2L, LocalDateTime.now(), LocalDateTime.now(), KeywordTypeEnum.Category, "Cat1"),
                        Keyword(3L, LocalDateTime.now(), LocalDateTime.now(), KeywordTypeEnum.Keyword, "kw1"),
                    ),
            )
        val urlSlot = slot<String>()
        val entitySlot = slot<HttpEntity<*>>()
        every {
            restTemplate.postForEntity(
                capture(urlSlot),
                capture(entitySlot),
                String::class.java,
            )
        } returns ResponseEntity.ok("{}")

        val result = target.publishSkill(rsd)

        assertThat(result.isSuccess).isTrue()
        assertThat(urlSlot.captured).endsWith("/assistant/competency/publish")
        @Suppress("UNCHECKED_CAST")
        val body = entitySlot.captured.body as String
        val json = objectMapper.readTree(body)
        val competencies = json.get("Competencies")
        assertThat(competencies.isArray).isTrue()
        assertThat(competencies.size()).isEqualTo(1)
        val competency = competencies.get(0)
        assertThat(competency.get("CTID").asText())
            .isEqualTo(ctidGenerator.generate(skillUuid))
        assertThat(competency.get("CompetencyLabel").asText()).isEqualTo("Test Skill")
        assertThat(competency.get("CompetencyText").asText()).isEqualTo("Can do X")
        assertThat(competency.get("PublicationStatusType").asText()).isEqualTo("Published")
        assertThat(competency.get("Creator").get(0).asText()).isEqualTo(orgCtid)
        assertThat(competency.has("Author")).isFalse()
        assertThat(competency.has("CompetencyCategory")).isFalse()
        assertThat(competency.get("ConceptKeyword").get(0).asText()).isEqualTo("kw1")
        assertThat(competency.get("ExactAlignment").get(0).asText())
            .isEqualTo("https://osmt.example.org/api/skills/$skillUuid")
    }

    @Test
    fun `publishSkill replaces ampersand with and in ConceptKeyword`() {
        val skillUuid = UUID.randomUUID().toString()
        val rsd =
            RichSkillDescriptor(
                id = 1L,
                creationDate = LocalDateTime.now(),
                updateDate = LocalDateTime.now(),
                uuid = skillUuid,
                name = "Test",
                statement = "Test",
                keywords =
                    listOf(
                        Keyword(
                            1L,
                            LocalDateTime.now(),
                            LocalDateTime.now(),
                            KeywordTypeEnum.Keyword,
                            "Curriculum & Instruction",
                        ),
                    ),
            )
        val entitySlot = slot<HttpEntity<*>>()
        every {
            restTemplate.postForEntity(
                any<String>(),
                capture(entitySlot),
                String::class.java,
            )
        } returns ResponseEntity.ok("{}")

        target.publishSkill(rsd)

        @Suppress("UNCHECKED_CAST")
        val body = entitySlot.captured.body as String
        val json = objectMapper.readTree(body)
        val kw = json.get("Competencies").get(0).get("ConceptKeyword")
        assertThat(kw.get(0).asText()).isEqualTo("Curriculum and Instruction")
    }

    @Test
    fun `deprecateSkill sends PublicationStatusType Deprecated`() {
        val skillUuid = UUID.randomUUID().toString()
        val rsd =
            RichSkillDescriptor(
                id = 1L,
                creationDate = LocalDateTime.now(),
                updateDate = LocalDateTime.now(),
                uuid = skillUuid,
                name = "Deprecated Skill",
                statement = "Old skill",
            )
        val entitySlot = slot<HttpEntity<*>>()
        every {
            restTemplate.postForEntity(
                any<String>(),
                capture(entitySlot),
                String::class.java,
            )
        } returns ResponseEntity.ok("{}")

        val result = target.deprecateSkill(rsd)

        assertThat(result.isSuccess).isTrue()
        @Suppress("UNCHECKED_CAST")
        val body = entitySlot.captured.body as String
        val json = objectMapper.readTree(body)
        val competency = json.get("Competencies").get(0)
        assertThat(competency.get("PublicationStatusType").asText())
            .isEqualTo("Deprecated")
    }

    @Test
    fun `publishCollection sends collection with HasMember`() {
        val collUuid = UUID.randomUUID().toString()
        val skillCtids = listOf("ce-s1", "ce-s2")
        val collection =
            edu.wgu.osmt.collection.Collection(
                id = 1L,
                creationDate = LocalDateTime.now(),
                updateDate = LocalDateTime.now(),
                uuid = collUuid,
                name = "Test Collection",
                description = "Desc",
                status = PublishStatus.Published,
            )
        val entitySlot = slot<HttpEntity<*>>()
        every {
            restTemplate.postForEntity(
                any<String>(),
                capture(entitySlot),
                String::class.java,
            )
        } returns ResponseEntity.ok("{}")

        val result = target.publishCollection(collection, skillCtids)

        assertThat(result.isSuccess).isTrue()
        @Suppress("UNCHECKED_CAST")
        val body = entitySlot.captured.body as String
        val json = objectMapper.readTree(body)
        val coll = json.get("Collection")
        assertThat(coll.get("CTID").asText())
            .isEqualTo(ctidGenerator.generate(collUuid))
        assertThat(coll.get("Name").asText()).isEqualTo("Test Collection")
        assertThat(coll.get("Description").asText()).isEqualTo("Desc")
        assertThat(coll.get("HasMember").get(0).asText()).isEqualTo("ce-s1")
        assertThat(coll.get("HasMember").get(1).asText()).isEqualTo("ce-s2")
        assertThat(coll.get("LifeCycleStatusType").asText()).isEqualTo("Active")
        assertThat(coll.get("SubjectWebpage").asText())
            .isEqualTo("https://osmt.example.org/api/collections/$collUuid")
    }

    @Test
    fun `deprecateCollection sends LifeCycleStatusType Ceased`() {
        val collUuid = UUID.randomUUID().toString()
        val collection =
            edu.wgu.osmt.collection.Collection(
                id = 1L,
                creationDate = LocalDateTime.now(),
                updateDate = LocalDateTime.now(),
                uuid = collUuid,
                name = "Old Collection",
                status = PublishStatus.Published,
            )
        val entitySlot = slot<HttpEntity<*>>()
        every {
            restTemplate.postForEntity(
                any<String>(),
                capture(entitySlot),
                String::class.java,
            )
        } returns ResponseEntity.ok("{}")

        val result = target.deprecateCollection(collection)

        assertThat(result.isSuccess).isTrue()
        @Suppress("UNCHECKED_CAST")
        val body = entitySlot.captured.body as String
        val json = objectMapper.readTree(body)
        val coll = json.get("Collection")
        assertThat(coll.get("LifeCycleStatusType").asText()).isEqualTo("Ceased")
        assertThat(coll.get("SubjectWebpage").asText())
            .isEqualTo("https://osmt.example.org/api/collections/$collUuid")
    }

    @Test
    fun `publishSkill returns failure on HTTP error`() {
        val rsd =
            RichSkillDescriptor(
                id = 1L,
                creationDate = LocalDateTime.now(),
                updateDate = LocalDateTime.now(),
                uuid = UUID.randomUUID().toString(),
                name = "X",
                statement = "Y",
            )
        every {
            restTemplate.postForEntity(any<String>(), any<HttpEntity<*>>(), String::class.java)
        } throws
            org.springframework.web.client.HttpClientErrorException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Bad Request",
                org.springframework.http.HttpHeaders().apply {
                    set("Content-Type", "application/json")
                },
                "{\"error\":\"invalid\"}".toByteArray(),
                java.nio.charset.StandardCharsets.UTF_8,
            )

        val result = target.publishSkill(rsd)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()!!.message).contains("400")
    }
}
