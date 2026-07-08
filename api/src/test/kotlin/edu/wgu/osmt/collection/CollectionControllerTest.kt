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
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Transactional
internal class CollectionControllerTest
    @Autowired
    constructor(
        val collectionRepository: CollectionRepository,
        val appConfig: AppConfig,
        override val collectionEsRepo: CollectionEsRepo,
        override val keywordEsRepo: KeywordEsRepo,
        override val jobCodeEsRepo: JobCodeEsRepo,
        override val richSkillEsRepo: RichSkillEsRepo,
    ) : SpringTest(),
        BaseDockerizedTest,
        HasDatabaseReset,
        HasElasticsearchReset {
        @Autowired
        lateinit var collectionController: CollectionController

        var authentication: Authentication = mockk()

        private lateinit var mockData: MockData

        val userString = "unittestuser"

        val userEmail = "unit@test.user"

        @BeforeAll
        fun setup() {
            ReflectionTestUtils.setField(appConfig, "roleAdmin", "ROLE_Osmt_Admin")
            val securityContext: SecurityContext = mockk()

            SecurityContextHolder.setContext(securityContext)

            val attributes: MutableMap<String, Any> = HashMap()
            attributes["email"] = userEmail

            val authority: GrantedAuthority = OAuth2UserAuthority("ROLE_Osmt_Admin", attributes)
            val authorities: MutableSet<GrantedAuthority> = HashSet()
            authorities.add(authority)

            every { securityContext.authentication }.returns(authentication)
            every { authentication.authorities }.returns(authorities)
        }

        @Test
        fun `workspaceByOwner() should retrieve an existing workspace`() {
            // arrange
            collectionRepository.create(
                CollectionUpdateObject(
                    12345,
                    "testCollection",
                    null,
                    null,
                    null,
                    PublishStatus.Workspace,
                ),
                userString,
                userEmail,
            )
            val jwt =
                Jwt
                    .withTokenValue("foo")
                    .header("foo", "foo")
                    .claim("email", userEmail)
                    .build()

            // act
            val result = collectionController.getOrCreateWorkspace(jwt)

            // assert
            Assertions.assertThat(result).isNotNull
        }

        @Test
        fun `workspaceByOwner() should create a workspace if it does not exist`() {
            // arrange
            val jwt =
                Jwt
                    .withTokenValue("foo")
                    .header("foo", "foo")
                    .claim("email", userEmail)
                    .build()

            // act
            Assertions.assertThat(collectionRepository.findAll().toList()).hasSize(0)
            val result = collectionController.getOrCreateWorkspace(jwt)

            // assert
            Assertions.assertThat(collectionRepository.findAll().toList()).hasSize(1)
            Assertions.assertThat(result).isNotNull
        }

        @Test
        fun `updateCollection() should return a collection to draft status when providing an Unarchived status and update the archived date`() {
            // arrange
            val jwt =
                Jwt
                    .withTokenValue("foo")
                    .header("foo", "foo")
                    .claim("email", userEmail)
                    .build()
            val update =
                ApiCollectionUpdate(
                    name = "newName",
                    description = "newDescription",
                    publishStatus = PublishStatus.Unarchived,
                    author = "newAuthor",
                )
            val collection =
                collectionRepository.create(
                    name = "name",
                    user = "user",
                    email = "user@xmail.com",
                    description = "description",
                )
            collection!!.status = PublishStatus.Archived
            collection.archiveDate = LocalDateTime.now()

            // act
            val result = collectionController.updateCollection(collection.uuid, update, jwt)

            // assert
            Assertions.assertThat(result).isNotNull
            Assertions.assertThat(result.status).isEqualTo(PublishStatus.Draft)
            Assertions.assertThat(result.archiveDate).isNull()
        }

        @Test
        fun `duplicateCollection should create a copy with metadata from the request`() {
            val jwt =
                Jwt
                    .withTokenValue("foo")
                    .header("foo", "foo")
                    .claim("email", userEmail)
                    .build()
            val collection =
                collectionRepository.create(
                    name = "Source Collection",
                    user = userString,
                    email = userEmail,
                    description = "desc",
                )!!
            val update =
                ApiCollectionUpdate(
                    name = "My Duplicate",
                    description = "desc2",
                    author = "author1",
                )
            val result =
                collectionController.duplicateCollection(collection.uuid, update, jwt)

            Assertions.assertThat(result.uuid).isNotEqualTo(collection.uuid)
            Assertions.assertThat(result.name).isEqualTo("My Duplicate")
            Assertions.assertThat(result.status).isEqualTo(PublishStatus.Draft)
        }

        @Test
        fun `duplicateCollection should return not found for unknown uuid`() {
            val jwt =
                Jwt
                    .withTokenValue("foo")
                    .header("foo", "foo")
                    .claim("email", userEmail)
                    .build()
            val ex =
                assertThrows<ResponseStatusException> {
                    collectionController.duplicateCollection(
                        "00000000-0000-0000-0000-000000000099",
                        ApiCollectionUpdate(
                            name = "n",
                            author = "a",
                        ),
                        jwt,
                    )
                }
            Assertions.assertThat(ex.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        }
    }
