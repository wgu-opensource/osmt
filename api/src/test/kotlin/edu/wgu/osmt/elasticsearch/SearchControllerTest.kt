package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.api.model.ApiAdvancedSearch
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.ApiSimilaritySearch
import edu.wgu.osmt.api.model.ApiSkillUpdate
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.mockdata.MockData
import edu.wgu.osmt.richskill.RichSkillDoc
import edu.wgu.osmt.richskill.RichSkillEsRepo
import edu.wgu.osmt.richskill.RichSkillRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder

@Transactional
internal class SearchControllerTest @Autowired constructor(
        override val richSkillEsRepo: RichSkillEsRepo,
        override val collectionEsRepo: CollectionEsRepo,
        override val keywordEsRepo: KeywordEsRepo,
        override val jobCodeEsRepo: JobCodeEsRepo,
        val richSkillRepository: RichSkillRepository
): SpringTest(), BaseDockerizedTest, HasDatabaseReset, HasElasticsearchReset {

    @Autowired
    lateinit var searchController: SearchController

    private lateinit var mockData : MockData
    val nullJwt : Jwt? = null

    @BeforeAll
    fun setup() {
        mockData = MockData()
    }

    @Test
    fun testSearchCollections(){
        // Arrange
        val collections = mockData.getCollections()
        val collectionDoc = mockData.getCollectionDoc(collections[0].uuid)

        collectionDoc?.let { collectionEsRepo.save(it) }

        // Act
        val result = searchController.searchCollections(
                UriComponentsBuilder.newInstance(),
                50,
                0,
                arrayOf("draft", "published", "workspace"),
                "",
                ApiSearch(advanced = ApiAdvancedSearch(collectionName = collectionDoc?.name)),
                nullJwt)

        // Assert
        assertThat(result.body?.first()?.uuid).isEqualTo(collectionDoc?.uuid)
    }

    @Test
    fun testSearchSkills(){
        // Arrange
        val listOfSkills = mockData.getRichSkillDocs().filter { !it.collections.isNullOrEmpty() }
        richSkillEsRepo.saveAll(listOfSkills)

        val collectionDoc = mockData.getCollectionDoc(listOfSkills[0].collections[0].uuid)
        collectionDoc?.let { collectionEsRepo.save(it) }

        // Act
        val result = searchController.searchSkills(
                UriComponentsBuilder.newInstance(),
                50,
                0,
                arrayOf("draft","published"),
                "",
                collectionDoc?.uuid,
                ApiSearch(query=listOfSkills[0].name),
                nullJwt)

        // Assert
        assertThat(result.body?.map { (it as RichSkillDoc).uuid }).contains(listOfSkills[0].uuid)
    }

    @Test
    fun testSearchJobCodes() {
        // Arrange
        val listOfJobCodes = mockData.getJobCodes()
        jobCodeEsRepo.saveAll(listOfJobCodes)

        // Act
        val result = searchController.searchJobCodes(UriComponentsBuilder.newInstance(),listOfJobCodes[0].code)

        // Assert
        assertThat(result.body?.map { it.targetNodeName }).contains(listOfJobCodes[0].name)
    }

    @Test
    fun testSearchKeywords() {
        // Arrange
        val listOfKeywords = mockData.getKeywords()
        keywordEsRepo.saveAll(listOfKeywords)

        // Act
        val result = searchController.searchKeywords(
                UriComponentsBuilder.newInstance(),
                listOfKeywords[0].value.toString(),
                listOfKeywords[0].type.toString())

        // Assert
        assertThat(result.body?.map { it.name }).contains(listOfKeywords[0].value)
    }

    @Test
    fun similarSkillWarningsShouldFindSimilarities() {
        richSkillRepository.createFromApi(
            listOf(ApiSkillUpdate(
                    "Access and Security Levels Standardization",
                    "Standardize levels of access and security to maintain information security.",
                    PublishStatus.Draft
                )),
            "admin",
            "admin@wgu.edu"
        )
        val response = searchController.similarSkillWarnings(
            arrayOf(ApiSimilaritySearch("Standardize levels of access and security to maintain information security."))
        )
        assertThat((response as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.size).isEqualTo(1)
        assertThat(response.body?.first()?.size).isEqualTo(1)
    }

    @Test
    fun similarSkillWarningsShouldNotFindSimilarities() {
        val response = searchController.similarSkillWarnings(
            arrayOf(ApiSimilaritySearch("Access an application programming interface (API) with a programming language to change data for a task."))
        )
        assertThat((response as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.size).isEqualTo(1)
        assertThat(response.body?.first()?.size).isEqualTo(0)
    }
}
