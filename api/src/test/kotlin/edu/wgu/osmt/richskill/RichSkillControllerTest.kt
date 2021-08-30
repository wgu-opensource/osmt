package edu.wgu.osmt.richskill

import edu.wgu.osmt.*
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.csv.BatchImportRichSkill
import edu.wgu.osmt.csv.RichSkillRow
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.mockdata.MockData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder

@Transactional
internal class RichSkillControllerTest @Autowired constructor(
        override val richSkillEsRepo: RichSkillEsRepo,
        override val collectionEsRepo: CollectionEsRepo,
        override val keywordEsRepo: KeywordEsRepo,
        override val jobCodeEsRepo: JobCodeEsRepo
): SpringTest(), BaseDockerizedTest, HasDatabaseReset, HasElasticsearchReset {

    @Autowired
    lateinit var richSkillController: RichSkillController

    @Autowired
    lateinit var batchImportRichSkill: BatchImportRichSkill

    private lateinit var mockData : MockData

    @BeforeAll
    fun setup() {
        mockData = MockData()
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
                "")

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
}