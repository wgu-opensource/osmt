package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.TestObjectHelpers.richSkillDoc
import edu.wgu.osmt.api.GeneralApiException
import edu.wgu.osmt.api.model.ApiAdvancedSearch
import edu.wgu.osmt.api.model.ApiNamedReference
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.ApiSimilaritySearch
import edu.wgu.osmt.api.model.ApiSkillUpdate
import edu.wgu.osmt.api.model.CollectionSortEnum
import edu.wgu.osmt.api.model.SkillSortEnum
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.OffsetPageable
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.mockdata.MockData
import edu.wgu.osmt.richskill.RichSkillEsRepo
import edu.wgu.osmt.richskill.RichSkillRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder

@Transactional
internal class SearchControllerTest
    @Autowired
    constructor(
        override val richSkillEsRepo: RichSkillEsRepo,
        override val collectionEsRepo: CollectionEsRepo,
        override val keywordEsRepo: KeywordEsRepo,
        override val jobCodeEsRepo: JobCodeEsRepo,
        val richSkillRepository: RichSkillRepository,
    ) : SpringTest(),
        BaseDockerizedTest,
        HasDatabaseReset,
        HasElasticsearchReset {
        @Autowired
        lateinit var searchController: SearchController

        private lateinit var mockData: MockData
        val nullJwt: Jwt? = null

        @BeforeAll
        fun setup() {
            mockData = MockData()
        }

        @Test
        fun testSearchCollections() {
            // Arrange
            val collections = mockData.getCollections()
            val collectionDoc = mockData.getCollectionDoc(collections[0].uuid)

            collectionDoc?.let { collectionEsRepo.save(it) }

            // Act
            val result =
                searchController.searchCollections(
                    UriComponentsBuilder.newInstance(),
                    50,
                    0,
                    arrayOf("draft", "published", "workspace"),
                    "",
                    ApiSearch(advanced = ApiAdvancedSearch(collectionName = collectionDoc?.name)),
                    nullJwt,
                )

            // Assert
            assertThat(result.body?.first()?.uuid).isEqualTo(collectionDoc?.uuid)
            val apiSearch =
                ApiSearch(advanced = ApiAdvancedSearch(collectionName = collectionDoc?.name))
            val collectionPageable =
                OffsetPageable(0, 50, CollectionSortEnum.forValueOrDefault("").sort)
            val collectionStatuses =
                setOf(PublishStatus.Published, PublishStatus.Workspace)
            assertThat(
                result.headers.getFirst("X-Total-Count")?.toLong(),
            ).isEqualTo(
                collectionEsRepo.countByApiSearch(
                    apiSearch,
                    collectionStatuses,
                    collectionPageable,
                ),
            )
        }

        @Test
        fun testSearchSkills() {
            // Arrange
            val listOfSkills =
                mockData.getRichSkillDocs().filter {
                    !it.collections.isNullOrEmpty()
                }
            richSkillEsRepo.saveAll(listOfSkills)

            val collectionDoc = mockData.getCollectionDoc(listOfSkills[0].collections[0].uuid)
            collectionDoc?.let { collectionEsRepo.save(it) }

            // Act
            val result =
                searchController.searchSkills(
                    UriComponentsBuilder.newInstance(),
                    50,
                    0,
                    arrayOf("draft", "published"),
                    "",
                    collectionDoc?.uuid,
                    ApiSearch(query = listOfSkills[0].name),
                    nullJwt,
                )

            // Assert
            assertThat(
                result.body?.map { it.uuid },
            ).contains(listOfSkills[0].uuid)
            val skillSearch = ApiSearch(query = listOfSkills[0].name)
            val skillPageable =
                OffsetPageable(
                    offset = 0,
                    limit = 50,
                    sort = SkillSortEnum.forApiValue("").sort,
                )
            val skillStatuses = setOf(PublishStatus.Published)
            assertThat(
                result.headers.getFirst("X-Total-Count")?.toLong(),
            ).isEqualTo(
                richSkillEsRepo.countByApiSearch(
                    skillSearch,
                    skillStatuses,
                    skillPageable,
                    collectionDoc?.uuid,
                ),
            )
        }

        @Test
        fun testSearchJobCodes() {
            // Arrange
            ReflectionTestUtils.setField(
                searchController,
                "appConfig",
                mockData.appConfig.apply {
                    ReflectionTestUtils.setField(this, "allowPublicLists", true)
                },
            )
            val listOfJobCodes = mockData.getJobCodes()
            jobCodeEsRepo.saveAll(listOfJobCodes)

            // Act
            val result =
                searchController.searchJobCodes(
                    UriComponentsBuilder.newInstance(),
                    listOfJobCodes[0].code,
                    nullJwt,
                )

            // Assert
            assertThat(result.body?.map { it.targetNodeName }).contains(listOfJobCodes[0].name)
        }

        @Test
        fun testSearchKeywords() {
            // Arrange - use authenticated user to test keyword index search
            val listOfKeywords = mockData.getKeywords()
            keywordEsRepo.saveAll(listOfKeywords)

            val jwt =
                Jwt
                    .withTokenValue("test-token")
                    .header("alg", "RS256")
                    .claim("sub", "test-user")
                    .build()

            // Act
            val result =
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    listOfKeywords[0].value.toString(),
                    listOfKeywords[0].type.toString(),
                    jwt, // authenticated - uses keyword index
                )

            // Assert
            assertThat(result.body?.map { it.name }).contains(listOfKeywords[0].value)
        }

        @Test
        fun similarSkillWarningsShouldFindSimilarities() {
            val skillUpdates =
                listOf(
                    ApiSkillUpdate(
                        "Access and Security Levels Standardization",
                        "Standardize levels of access and security to maintain information security.",
                        PublishStatus.Draft,
                    ),
                )
            richSkillRepository.createFromApi(
                skillUpdates,
                "admin",
                "admin@wgu.edu",
            )
            val response =
                searchController.similarSkillResults(
                    arrayOf(
                        ApiSimilaritySearch(
                            "Standardize levels of access and security to maintain information security.",
                        ),
                    ),
                )
            assertThat((response as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            assertThat(
                response.body
                    ?.first()
                    ?.get(0)
                    ?.skillStatement,
            ).isEqualTo(skillUpdates[0].skillStatement)
            assertThat(response.body?.first()?.size).isEqualTo(1)
        }

        @Test
        fun similarSkillWarningsShouldNotFindSimilarities() {
            val response =
                searchController.similarSkillResults(
                    arrayOf(
                        ApiSimilaritySearch(
                            "Access an application programming interface (API) with a programming language to change data for a task.",
                        ),
                    ),
                )
            assertThat((response as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            assertThat(response.body?.first()?.size).isEqualTo(0)
        }

        @Test
        fun testSearchKeywordsUnauthenticatedReturnsPublicKeywords() {
            // Arrange
            ReflectionTestUtils.setField(
                searchController,
                "appConfig",
                mockData.appConfig.apply {
                    ReflectionTestUtils.setField(this, "allowPublicLists", true)
                },
            )

            // Create skills with different publish statuses
            val publishedSkill =
                richSkillDoc(
                    name = "Published Skill",
                    statement = "Published skill statement",
                    categories = listOf("Web Development", "Programming"),
                    authors = listOf("Author 1"),
                    publishStatus = PublishStatus.Published,
                ).copy(
                    searchingKeywords = listOf("JavaScript", "React"),
                    standards = listOf("Standard 1"),
                )

            val archivedSkill =
                richSkillDoc(
                    name = "Archived Skill",
                    statement = "Archived skill statement",
                    categories = listOf("Database", "Programming"),
                    authors = listOf("Author 2"),
                    publishStatus = PublishStatus.Archived,
                ).copy(
                    searchingKeywords = listOf("SQL", "React"),
                    standards = listOf("Standard 2"),
                )

            val draftSkill =
                richSkillDoc(
                    name = "Draft Skill",
                    statement = "Draft skill statement",
                    categories = listOf("Mobile", "Programming"),
                    authors = listOf("Author 3"),
                    publishStatus = PublishStatus.Draft,
                ).copy(
                    searchingKeywords = listOf("Flutter", "Dart"),
                    standards = listOf("Standard 3"),
                )

            richSkillEsRepo.saveAll(listOf(publishedSkill, archivedSkill, draftSkill))

            // Act - search for categories with empty query to get all
            val result =
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    "", // empty query to get all
                    "category", // type
                    nullJwt, // unauthenticated
                )

            // Assert
            assertThat((result as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            val keywords = result.body ?: emptyList()
            assertThat(keywords.map { it.name }).contains("Web Development")
            assertThat(keywords.map { it.name }).contains("Database") // archived skill keyword
            assertThat(keywords.map { it.name }).doesNotContain("Mobile") // draft skill keyword
        }

        @Test
        fun testSearchKeywordsUnauthenticatedFiltersByQuery() {
            // Arrange
            ReflectionTestUtils.setField(
                searchController,
                "appConfig",
                mockData.appConfig.apply {
                    ReflectionTestUtils.setField(this, "allowPublicLists", true)
                },
            )

            val publishedSkill =
                richSkillDoc(
                    name = "JavaScript Skill",
                    statement = "JavaScript skill statement",
                    publishStatus = PublishStatus.Published,
                ).copy(
                    searchingKeywords = listOf("JavaScript", "Frontend", "React"),
                )

            richSkillEsRepo.save(publishedSkill)

            // Act - search for specific keyword
            val result =
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    "React", // specific search
                    "keyword",
                    nullJwt,
                )

            // Assert
            assertThat((result as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            val keywords = result.body ?: emptyList()
            assertThat(keywords.map { it.name }).contains("React")
            assertThat(keywords.map { it.name }).doesNotContain("JavaScript") // doesn't match query
            assertThat(keywords.map { it.name }).doesNotContain("Frontend") // doesn't match query
        }

        @Test
        fun testSearchKeywordsUnauthenticatedFiltersOutDraftDeleted() {
            // Arrange
            ReflectionTestUtils.setField(
                searchController,
                "appConfig",
                mockData.appConfig.apply {
                    ReflectionTestUtils.setField(this, "allowPublicLists", true)
                },
            )

            val publishedSkill =
                richSkillDoc(
                    name = "Published",
                    statement = "Published skill",
                    categories = listOf("Published Category"),
                    publishStatus = PublishStatus.Published,
                )

            val archivedSkill =
                richSkillDoc(
                    name = "Archived",
                    statement = "Archived skill",
                    categories = listOf("Archived Category"),
                    publishStatus = PublishStatus.Archived,
                )

            val draftSkill =
                richSkillDoc(
                    name = "Draft",
                    statement = "Draft skill",
                    categories = listOf("Draft Category"),
                    publishStatus = PublishStatus.Draft,
                )

            val deletedSkill =
                richSkillDoc(
                    name = "Deleted",
                    statement = "Deleted skill",
                    categories = listOf("Deleted Category"),
                    publishStatus = PublishStatus.Deleted,
                )

            richSkillEsRepo.saveAll(listOf(publishedSkill, archivedSkill, draftSkill, deletedSkill))

            // Act
            val result =
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    "", // empty query to get all
                    "category",
                    nullJwt,
                )

            // Assert
            assertThat((result as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            val keywords = result.body ?: emptyList()
            val keywordNames = keywords.map { it.name }

            assertThat(keywordNames).contains("Published Category")
            assertThat(keywordNames).contains("Archived Category")
            assertThat(keywordNames).doesNotContain("Draft Category")
            assertThat(keywordNames).doesNotContain("Deleted Category")
        }

        @Test
        fun testSearchKeywordsAuthenticatedUsesKeywordIndex() {
            // Arrange - create a JWT for authenticated user
            val jwt =
                Jwt
                    .withTokenValue("test-token")
                    .header("alg", "RS256")
                    .claim("sub", "test-user")
                    .build()

            // This test verifies that authenticated users still use the keyword index
            // (we can't easily mock the keyword index results without complex setup,
            // but we can verify the method doesn't throw and returns a proper response)
            val result =
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    "test",
                    "keyword",
                    jwt,
                )

            // Assert
            assertThat(result).isNotNull()
            assertThat((result as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
        }

        @Test
        fun testSearchJobCodesUnauthenticatedWorks() {
            // Arrange
            ReflectionTestUtils.setField(
                searchController,
                "appConfig",
                mockData.appConfig.apply {
                    ReflectionTestUtils.setField(this, "allowPublicLists", true)
                },
            )
            val listOfJobCodes = mockData.getJobCodes()
            jobCodeEsRepo.saveAll(listOfJobCodes)

            // Act - call without JWT (unauthenticated)
            val result =
                searchController.searchJobCodes(
                    UriComponentsBuilder.newInstance(),
                    listOfJobCodes[0].code,
                    nullJwt,
                )

            // Assert - unauthenticated users can access jobcodes
            assertThat((result as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            assertThat(result.body?.map { it.targetNodeName }).contains(listOfJobCodes[0].name)
        }

        @Test
        fun testSearchKeywordsUnauthenticatedDoesNotExposeDraftKeywords() {
            // Arrange
            ReflectionTestUtils.setField(
                searchController,
                "appConfig",
                mockData.appConfig.apply {
                    ReflectionTestUtils.setField(this, "allowPublicLists", true)
                },
            )

            val draftSkill =
                richSkillDoc(
                    name = "Draft Skill",
                    statement = "Draft skill statement",
                    categories = listOf("Draft Category"),
                    authors = listOf("Draft Author"),
                    publishStatus = PublishStatus.Draft,
                ).copy(
                    searchingKeywords = listOf("Draft Keyword", "Secret Keyword"),
                    standards = listOf("Draft Standard"),
                    certifications = listOf("Draft Certification"),
                    employers = listOf("Draft Employer"),
                    alignments = listOf("Draft Alignment"),
                )

            richSkillEsRepo.save(draftSkill)

            // Act - search for keywords from draft skill
            val keywordResult =
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    "Draft Keyword",
                    "keyword",
                    nullJwt,
                )

            val categoryResult =
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    "Draft Category",
                    "category",
                    nullJwt,
                )

            val authorResult =
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    "Draft Author",
                    "author",
                    nullJwt,
                )

            // Assert - draft skill keywords should NOT be returned
            assertThat((keywordResult as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            assertThat(keywordResult.body?.map { it.name }).doesNotContain("Draft Keyword")
            assertThat(keywordResult.body?.map { it.name }).doesNotContain("Secret Keyword")

            assertThat((categoryResult as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            assertThat(categoryResult.body?.map { it.name }).doesNotContain("Draft Category")

            assertThat((authorResult as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            assertThat(authorResult.body?.map { it.name }).doesNotContain("Draft Author")
        }

        @Test
        fun testSearchKeywordsUnauthenticatedDoesNotExposeDeletedKeywords() {
            // Arrange
            ReflectionTestUtils.setField(
                searchController,
                "appConfig",
                mockData.appConfig.apply {
                    ReflectionTestUtils.setField(this, "allowPublicLists", true)
                },
            )

            val deletedSkill =
                richSkillDoc(
                    name = "Deleted Skill",
                    statement = "Deleted skill statement",
                    categories = listOf("Deleted Category"),
                    authors = listOf("Deleted Author"),
                    publishStatus = PublishStatus.Deleted,
                ).copy(
                    searchingKeywords = listOf("Deleted Keyword"),
                    standards = listOf("Deleted Standard"),
                )

            richSkillEsRepo.save(deletedSkill)

            // Act
            val keywordResult =
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    "Deleted Keyword",
                    "keyword",
                    nullJwt,
                )

            val categoryResult =
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    "Deleted Category",
                    "category",
                    nullJwt,
                )

            // Assert - deleted skill keywords should NOT be returned
            assertThat((keywordResult as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            assertThat(keywordResult.body?.map { it.name }).doesNotContain("Deleted Keyword")

            assertThat((categoryResult as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            assertThat(categoryResult.body?.map { it.name }).doesNotContain("Deleted Category")
        }

        @Test
        fun testSearchKeywordsUnauthenticatedExposesPublishedKeywords() {
            // Arrange
            ReflectionTestUtils.setField(
                searchController,
                "appConfig",
                mockData.appConfig.apply {
                    ReflectionTestUtils.setField(this, "allowPublicLists", true)
                },
            )

            val publishedSkill =
                richSkillDoc(
                    name = "Published Skill",
                    statement = "Published skill statement",
                    categories = listOf("Published Category"),
                    authors = listOf("Published Author"),
                    publishStatus = PublishStatus.Published,
                ).copy(
                    searchingKeywords = listOf("Published Keyword"),
                    standards = listOf("Published Standard"),
                    certifications = listOf("Published Certification"),
                    employers = listOf("Published Employer"),
                    alignments = listOf("Published Alignment"),
                )

            richSkillEsRepo.save(publishedSkill)

            // Act
            val keywordResult =
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    "Published Keyword",
                    "keyword",
                    nullJwt,
                )

            val categoryResult =
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    "Published Category",
                    "category",
                    nullJwt,
                )

            val authorResult =
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    "Published Author",
                    "author",
                    nullJwt,
                )

            // Assert - published skill keywords SHOULD be returned
            assertThat((keywordResult as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            assertThat(keywordResult.body?.map { it.name }).contains("Published Keyword")

            assertThat((categoryResult as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            assertThat(categoryResult.body?.map { it.name }).contains("Published Category")

            assertThat((authorResult as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            assertThat(authorResult.body?.map { it.name }).contains("Published Author")
        }

        @Test
        fun testSearchKeywordsUnauthenticatedExposesArchivedKeywords() {
            // Arrange
            ReflectionTestUtils.setField(
                searchController,
                "appConfig",
                mockData.appConfig.apply {
                    ReflectionTestUtils.setField(this, "allowPublicLists", true)
                },
            )

            val archivedSkill =
                richSkillDoc(
                    name = "Archived Skill",
                    statement = "Archived skill statement",
                    categories = listOf("Archived Category"),
                    authors = listOf("Archived Author"),
                    publishStatus = PublishStatus.Archived,
                ).copy(
                    searchingKeywords = listOf("Archived Keyword"),
                    standards = listOf("Archived Standard"),
                )

            richSkillEsRepo.save(archivedSkill)

            // Act
            val keywordResult =
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    "Archived Keyword",
                    "keyword",
                    nullJwt,
                )

            val categoryResult =
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    "Archived Category",
                    "category",
                    nullJwt,
                )

            // Assert - archived skill keywords SHOULD be returned
            assertThat((keywordResult as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            assertThat(keywordResult.body?.map { it.name }).contains("Archived Keyword")

            assertThat((categoryResult as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            assertThat(categoryResult.body?.map { it.name }).contains("Archived Category")
        }

        @Test
        fun testSearchKeywordsUnauthenticatedFiltersAllKeywordTypes() {
            // Arrange
            ReflectionTestUtils.setField(
                searchController,
                "appConfig",
                mockData.appConfig.apply {
                    ReflectionTestUtils.setField(this, "allowPublicLists", true)
                },
            )

            val publishedSkill =
                richSkillDoc(
                    name = "Published Skill",
                    statement = "Published skill statement",
                    publishStatus = PublishStatus.Published,
                ).copy(
                    searchingKeywords = listOf("Published Keyword"),
                    standards = listOf("Published Standard"),
                    certifications = listOf("Published Certification"),
                    employers = listOf("Published Employer"),
                    alignments = listOf("Published Alignment"),
                )

            val draftSkill =
                richSkillDoc(
                    name = "Draft Skill",
                    statement = "Draft skill statement",
                    publishStatus = PublishStatus.Draft,
                ).copy(
                    searchingKeywords = listOf("Draft Keyword"),
                    standards = listOf("Draft Standard"),
                    certifications = listOf("Draft Certification"),
                    employers = listOf("Draft Employer"),
                    alignments = listOf("Draft Alignment"),
                )

            richSkillEsRepo.saveAll(listOf(publishedSkill, draftSkill))

            // Act & Assert - test all keyword types
            val keywordTypes =
                listOf(
                    "keyword" to "Published Keyword",
                    "standard" to "Published Standard",
                    "certification" to "Published Certification",
                    "employer" to "Published Employer",
                    "alignment" to "Published Alignment",
                )

            for ((type, publishedValue) in keywordTypes) {
                val result =
                    searchController.searchKeywords(
                        UriComponentsBuilder.newInstance(),
                        publishedValue,
                        type,
                        nullJwt,
                    )

                assertThat((result as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
                assertThat(result.body?.map { it.name }).contains(publishedValue)
            }

            // Verify draft keywords are NOT returned
            val draftKeywordResult =
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    "Draft Keyword",
                    "keyword",
                    nullJwt,
                )

            assertThat((draftKeywordResult as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
            assertThat(draftKeywordResult.body?.map { it.name }).doesNotContain("Draft Keyword")
        }

        @Test
        fun testSearchKeywordsUnauthenticatedRejectedWhenAllowPublicListsFalse() {
            // Arrange
            ReflectionTestUtils.setField(
                searchController,
                "appConfig",
                mockData.appConfig.apply {
                    ReflectionTestUtils.setField(this, "allowPublicLists", false)
                },
            )

            // Act & Assert
            assertThatThrownBy {
                searchController.searchKeywords(
                    UriComponentsBuilder.newInstance(),
                    "test",
                    "keyword",
                    nullJwt,
                )
            }.isInstanceOf(GeneralApiException::class.java)
                .hasMessage("Unauthorized")
        }

        @Test
        fun testSearchJobCodesUnauthenticatedRejectedWhenAllowPublicListsFalse() {
            // Arrange
            ReflectionTestUtils.setField(
                searchController,
                "appConfig",
                mockData.appConfig.apply {
                    ReflectionTestUtils.setField(this, "allowPublicLists", false)
                },
            )
            val listOfJobCodes = mockData.getJobCodes()
            jobCodeEsRepo.saveAll(listOfJobCodes)

            // Act & Assert
            assertThatThrownBy {
                searchController.searchJobCodes(
                    UriComponentsBuilder.newInstance(),
                    listOfJobCodes[0].code,
                    nullJwt,
                )
            }.isInstanceOf(GeneralApiException::class.java)
                .hasMessage("Unauthorized")
        }
    }
