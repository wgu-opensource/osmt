package edu.wgu.osmt.richskill

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.RoutePaths.EXPORT_LIBRARY
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.csv.BatchImportRichSkill
import edu.wgu.osmt.csv.RichSkillRow
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.mockdata.MockData
import edu.wgu.osmt.security.OAuthHelper
import edu.wgu.osmt.task.CsvTask
import edu.wgu.osmt.task.Task
import edu.wgu.osmt.task.TaskMessageService
import edu.wgu.osmt.task.TaskResult
import edu.wgu.osmt.task.TaskStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder
import java.time.Instant
import java.util.*


@Transactional
internal class RichSkillControllerTest @Autowired constructor(
        override val richSkillEsRepo: RichSkillEsRepo,
        val taskMessageService: TaskMessageService,
        val oAuthHelper: OAuthHelper,
        val appConfig: AppConfig,
        override val collectionEsRepo: CollectionEsRepo,
        override val keywordEsRepo: KeywordEsRepo,
        override val jobCodeEsRepo: JobCodeEsRepo
): SpringTest(), BaseDockerizedTest, HasDatabaseReset, HasElasticsearchReset {

    var authentication: Authentication = Mockito.mock(Authentication::class.java)

    @Autowired
    lateinit var richSkillController: RichSkillController

    @Autowired
    lateinit var batchImportRichSkill: BatchImportRichSkill

    private lateinit var mockData : MockData
    val nullJwt : Jwt? = null


    @BeforeAll
    fun setup() {
        mockData = MockData()
        ReflectionTestUtils.setField(appConfig, "roleAdmin", "ROLE_Osmt_Admin");
    }

    @Test
    fun testAllPaginated(){
        // Arrange
        val size = 50
        val listOfSkills = mockData.getRichSkillDocs()
        richSkillEsRepo.saveAll(listOfSkills)

        // Act
        val result = richSkillController.allPaginated(
                UriComponentsBuilder.newInstance(),
                size,
                0,
                arrayOf("draft","published"),
                "",
                nullJwt
        )

        // Assert
        assertThat(result.body?.size).isEqualTo(size)
    }

    @Test
    fun testByUUID(){
        // Arrange
        val numOfSkills = 3
        val richSkillRows = mockData.getRichSkillRows()
        val listOfRichSkillRows = mutableListOf<RichSkillRow>()
        val jwt = Jwt.withTokenValue("foo").header("foo", "foo").claim("foo", "foo").build()

        for (i in 1..numOfSkills ) {
            listOfRichSkillRows.add(richSkillRows[i])
        }

        batchImportRichSkill.handleRows(listOfRichSkillRows)

        // Act
        val skillResult = richSkillEsRepo.byApiSearch(ApiSearch())
        val result = richSkillController.byUUID(skillResult.searchHits[0].id.toString(),jwt)

        // Assert
        assertThat(result?.uuid).isEqualTo(skillResult.searchHits[0].id.toString())
    }

    @Test
    fun testByUUIDHtmlView(){
        // Arrange
        val numOfSkills = 3
        val richSkillRows = mockData.getRichSkillRows()
        val listOfRichSkillRows = mutableListOf<RichSkillRow>()
        val jwt = Jwt.withTokenValue("foo").header("foo", "foo").claim("foo", "foo").build()

        for (i in 1..numOfSkills ) {
            listOfRichSkillRows.add(richSkillRows[i])
        }

        batchImportRichSkill.handleRows(listOfRichSkillRows)

        // Act
        val skillResult = richSkillEsRepo.byApiSearch(ApiSearch())
        val result = richSkillController.byUUIDHtmlView(skillResult.searchHits[0].id.toString(),jwt)

        // Assert
        assertThat(result).isEqualTo("forward:/skills/"+skillResult.searchHits[0].id.toString())
    }

    @Test
    fun testByUUIDCsvView(){
        // Arrange
        val numOfSkills = 3
        val richSkillRows = mockData.getRichSkillRows()
        val listOfRichSkillRows = mutableListOf<RichSkillRow>()
        val jwt = Jwt.withTokenValue("foo").header("foo", "foo").claim("foo", "foo").build()

        for (i in 1..numOfSkills ) {
            listOfRichSkillRows.add(richSkillRows[i])
        }

        batchImportRichSkill.handleRows(listOfRichSkillRows)

        // Act
        val skillResult = richSkillEsRepo.byApiSearch(ApiSearch())
        val result = richSkillController.byUUIDCsvView(skillResult.searchHits[0].id.toString(),jwt)

        // Assert
        assertThat(result.body.toString()).contains(skillResult.searchHits[0].id.toString())
    }

    @Test
    fun testSkillAuditLog(){
        // Arrange
        val numOfSkills = 3
        val richSkillRows = mockData.getRichSkillRows()
        val listOfRichSkillRows = mutableListOf<RichSkillRow>()

        for (i in 1..numOfSkills ) {
            listOfRichSkillRows.add(richSkillRows[i])
        }

        batchImportRichSkill.handleRows(listOfRichSkillRows)

        // Act
        val skillResult = richSkillEsRepo.byApiSearch(ApiSearch())
        val result = richSkillController.skillAuditLog(skillResult.searchHits[0].id.toString())

        // Assert
        assertThat(result.body?.get(0)?.operationType).isEqualTo("Insert")
        assertThat(result.body?.get(0)?.user).isEqualTo("Batch Import")
    }


    @Disabled
    @Test
    fun testExportLibrary() {

        val securityContext: SecurityContext = Mockito.mock(SecurityContext::class.java)
        SecurityContextHolder.setContext(securityContext)

        val attributes: MutableMap<String, Any> = HashMap()
        attributes["email"] = "j.chavez@wgu.edu"

        val authority: GrantedAuthority = OAuth2UserAuthority("ROLE_Osmt_Admin", attributes)
        val authorities: MutableSet<GrantedAuthority> = HashSet()
        authorities.add(authority)
        Mockito.`when`(securityContext.authentication).thenReturn(authentication)
        Mockito.`when`(SecurityContextHolder.getContext().authentication.authorities).thenReturn(authorities)


        val responseHeaders = HttpHeaders()
        responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        val headers : MutableMap<String, Any> = HashMap()
        headers["key"] = "value"
        val notNullJwt : Jwt? = Jwt("tokenValue", Instant.MIN, Instant.MAX,headers,headers)
        val csvTaskResult = TaskResult(UUID.randomUUID().toString(),MediaType.APPLICATION_JSON_VALUE,TaskStatus.Processing, EXPORT_LIBRARY)


        val service = mockk<TaskMessageService>()
        every { service.enqueueJob(any(), any()) } returns Unit
        mockkStatic(CsvTask::class)
        mockkStatic(TaskResult::class)
        every { Task.processingResponse(any()) } returns HttpEntity(csvTaskResult)

        val result = richSkillController.exportLibrary(user = notNullJwt)
        assertThat(result.body?.uuid).isNotBlank()
    }



}