package edu.wgu.osmt.collection

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.mockdata.MockData
import edu.wgu.osmt.richskill.RichSkillEsRepo
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.transaction.annotation.Transactional

@Transactional
internal class CollectionControllerTest @Autowired constructor(
    val collectionRepository: CollectionRepository,
    val appConfig: AppConfig,
    override val collectionEsRepo: CollectionEsRepo,
    override val keywordEsRepo: KeywordEsRepo,
    override val jobCodeEsRepo: JobCodeEsRepo,
    override val richSkillEsRepo: RichSkillEsRepo
): SpringTest(), BaseDockerizedTest, HasDatabaseReset, HasElasticsearchReset {

    @Autowired
    lateinit var collectionController: CollectionController

    private lateinit var mockData : MockData

    val userString = "unittestuser"

    val userEmail = "unit@test.user"


    @BeforeAll
    fun setup() {
        mockData = MockData()
        ReflectionTestUtils.setField(appConfig, "roleAdmin", "ROLE_Osmt_Admin")
    }

    @Test
    fun `workspaceByOwner() should retrieve an existing workspace`() {
        // arrange
        collectionRepository.create(CollectionUpdateObject(12345,"testCollection",null,null,null,PublishStatus.Workspace), userString, userEmail)
        val jwt = Jwt.withTokenValue("foo").header("foo", "foo").claim("email", userEmail).build()

        // act
        val result = collectionController.getOrCreateWorkspace(jwt)

        // assert
        Assertions.assertThat(result).isNotNull
    }

    @Test
    fun `workspaceByOwner() should create a workspace if it does not exist`() {
        // arrange
        val jwt = Jwt.withTokenValue("foo").header("foo", "foo").claim("email", userEmail).build()

        // act
        Assertions.assertThat(collectionRepository.findAll().toList()).hasSize(0)
        val result = collectionController.getOrCreateWorkspace(jwt)

        // assert
        Assertions.assertThat(collectionRepository.findAll().toList()).hasSize(1)
        Assertions.assertThat(result).isNotNull
    }
}