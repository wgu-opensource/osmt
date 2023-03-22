package edu.wgu.osmt.collection

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.api.model.ApiCollectionUpdate
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.mockdata.MockData
import edu.wgu.osmt.richskill.RichSkillEsRepo
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
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

    var authentication: Authentication = Mockito.mock(Authentication::class.java)

    private lateinit var mockData : MockData

    val userString = "unittestuser"

    val userEmail = "unit@test.user"


    @BeforeAll
    fun setup() {
        mockData = MockData()
        ReflectionTestUtils.setField(appConfig, "roleAdmin", "ROLE_Osmt_Admin")
        val securityContext: SecurityContext = Mockito.mock(SecurityContext::class.java)
        SecurityContextHolder.setContext(securityContext)

        val attributes: MutableMap<String, Any> = HashMap()
        attributes["email"] = userEmail

        val authority: GrantedAuthority = OAuth2UserAuthority("ROLE_Osmt_Admin", attributes)
        val authorities: MutableSet<GrantedAuthority> = HashSet()
        authorities.add(authority)
        Mockito.`when`(securityContext.authentication).thenReturn(authentication)
        Mockito.`when`(SecurityContextHolder.getContext().authentication.authorities).thenReturn(authorities)
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

    @Test
    fun `updateCollection() should change the collection status when providing an Archived status and update the archived date`() {
        // arrange
        val jwt = Jwt.withTokenValue("foo").header("foo", "foo").claim("email", userEmail).build()
        val update = ApiCollectionUpdate(
            name = "newName",
            description = "newDescription",
            publishStatus = PublishStatus.Archived,
            author = "newAuthor")
        val collectionUUID = collectionRepository.create(name = "name", user = "user", email = "j.chavez@wgu.edu", description = "description")!!.uuid

        // act
        Assertions.assertThat(collectionRepository.findByUUID(collectionUUID)!!.status).isEqualTo(PublishStatus.Draft)
        Assertions.assertThat(collectionRepository.findByUUID(collectionUUID)!!.archiveDate).isNull()
        val result = collectionController.updateCollection(collectionUUID, update, jwt)

        // assert
        Assertions.assertThat(result).isNotNull
        Assertions.assertThat(result.status).isEqualTo(PublishStatus.Archived)
        Assertions.assertThat(result.archiveDate).isNotNull
    }

    @Test
    fun `updateCollection() should return a collection to draft status when providing an Unarchived status and update the archived date`() {
        // arrange
        val jwt = Jwt.withTokenValue("foo").header("foo", "foo").claim("email", userEmail).build()
        val update = ApiCollectionUpdate(
            name = "newName",
            description = "newDescription",
            publishStatus = PublishStatus.Unarchived,
            author = "newAuthor")
        val collection = collectionRepository.create(name = "name", user = "user", email = "j.chavez@wgu.edu", description = "description")
        collection!!.status = PublishStatus.Archived

        // act
        Assertions.assertThat(collectionRepository.findByUUID(collection.uuid)!!.status).isEqualTo(PublishStatus.Archived)
        Assertions.assertThat(collectionRepository.findByUUID(collection.uuid)!!.archiveDate).isNull()
        val result = collectionController.updateCollection(collection.uuid, update, jwt)

        // assert
        Assertions.assertThat(result).isNotNull
        Assertions.assertThat(result.status).isEqualTo(PublishStatus.Draft)
        Assertions.assertThat(result.archiveDate).isNull()
    }

    @Test
    fun `updateCollection() should return a collection to published status when providing an Unarchived status and update the archived and published date`() {
        // arrange
        val jwt = Jwt.withTokenValue("foo").header("foo", "foo").claim("email", userEmail).build()
        var update = ApiCollectionUpdate(
            name = "newName",
            description = "newDescription",
            publishStatus = PublishStatus.Published,
            author = "newAuthor")
        val collection = collectionRepository.create(name = "name", user = "user", email = "j.chavez@wgu.edu", description = "description")

        // act
        Assertions.assertThat(collectionRepository.findByUUID(collection!!.uuid)!!.status).isEqualTo(PublishStatus.Draft)
        Assertions.assertThat(collectionRepository.findByUUID(collection.uuid)!!.archiveDate).isNull()
        collectionController.updateCollection(collection.uuid, update, jwt)
        Assertions.assertThat(collectionRepository.findByUUID(collection.uuid)!!.status).isEqualTo(PublishStatus.Published)
        Assertions.assertThat(collectionRepository.findByUUID(collection.uuid)!!.publishDate).isNotNull()
        update = ApiCollectionUpdate(
            name = "newName",
            description = "newDescription",
            publishStatus = PublishStatus.Archived,
            author = "newAuthor")
        collectionController.updateCollection(collection.uuid, update, jwt)
        Assertions.assertThat(collectionRepository.findByUUID(collection.uuid)!!.status).isEqualTo(PublishStatus.Archived)
        Assertions.assertThat(collectionRepository.findByUUID(collection.uuid)!!.archiveDate).isNotNull
        update = ApiCollectionUpdate(
            name = "newName",
            description = "newDescription",
            publishStatus = PublishStatus.Unarchived,
            author = "newAuthor")
        val result = collectionController.updateCollection(collection.uuid, update, jwt)

        // assert
        Assertions.assertThat(result).isNotNull
        Assertions.assertThat(result.status).isEqualTo(PublishStatus.Published)
        Assertions.assertThat(result.archiveDate).isNull()
        Assertions.assertThat(result.publishDate).isNotNull
    }
}