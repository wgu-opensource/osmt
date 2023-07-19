package edu.wgu.osmt.keyword

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.api.model.ApiKeyword
import edu.wgu.osmt.api.model.ApiKeywordUpdate
import edu.wgu.osmt.api.model.KeywordSortEnum
import edu.wgu.osmt.api.model.SkillSortEnum
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.mockdata.MockData
import edu.wgu.osmt.richskill.RichSkillEsRepo
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.richskill.RsdUpdateObject
import io.mockk.spyk
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
internal class KeywordControllerTest @Autowired constructor(
    val keywordRepository: KeywordRepository,
    val richSkillRepository: RichSkillRepository,
    override val collectionEsRepo: CollectionEsRepo,
    override val jobCodeEsRepo: JobCodeEsRepo,
    override val keywordEsRepo: KeywordEsRepo,
    override val richSkillEsRepo: RichSkillEsRepo
): SpringTest(), BaseDockerizedTest, HasDatabaseReset, HasElasticsearchReset {

    @Autowired
    lateinit var kwController: KeywordController

    private lateinit var mockData : MockData

    private lateinit var mockKeywordRepository: KeywordRepository

    val nullJwt : Jwt? = null


    @BeforeAll
    fun setup() {
        mockData = MockData()
        mockKeywordRepository = spyk()
    }

    @Test
    fun `allPaginated() should retrieve an existing keyword`() {
        // arrange
        val size: Int = 2
        val from: Int = 2
        val sort: KeywordSortEnum = KeywordSortEnum.KeywordNameAsc

        mockData.getKeywords().map {
            keywordRepository.create(it.type, it.value, it.uri, it.framework)
        }

        // act
        val result = kwController.allPaginated(
            uriComponentsBuilder = UriComponentsBuilder.newInstance(),
            type = KeywordTypeEnum.Category.toString(),
            org.apache.commons.lang3.StringUtils.EMPTY,
            size = size,
            from = from,
            sort = sort.toString(),
        )

        // assert
        Assertions.assertThat(result!!).isNotNull
        Assertions.assertThat(result.body?.size).isEqualTo(size)
        Assertions.assertThat(result).isExactlyInstanceOf(ResponseEntity::class.java)
        Assertions.assertThat((result as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `keywordById() should retrieve an existing keyword`() {
        // arrange
        val keyword = keywordRepository.create(KeywordTypeEnum.Category, "category1")

        // act
        val result = keyword?.id?.let { kwController.keywordById(it.value) }

        // assert
        Assertions.assertThat(result).isNotNull
        Assertions.assertThat(result).isExactlyInstanceOf(ResponseEntity::class.java)
        Assertions.assertThat(result!!.body).isExactlyInstanceOf(ApiKeyword::class.java)
        Assertions.assertThat((result as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `createKeyword() should create a new keyword`() {
        // act
        val result = kwController.createKeyword(ApiKeywordUpdate("name","uri",KeywordTypeEnum.Category, "framework"), nullJwt)

        // assert
        Assertions.assertThat(result).isNotNull
        Assertions.assertThat(result).isExactlyInstanceOf(ResponseEntity::class.java)
        Assertions.assertThat(result.body).isExactlyInstanceOf(ApiKeyword::class.java)
        Assertions.assertThat(result.body!!.name).isEqualTo("name")
        Assertions.assertThat(result.body!!.framework).isEqualTo("framework")
        Assertions.assertThat((result as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `updateKeyword() should update an existing keyword`() {
        //arrange
        val keyword = keywordRepository.create(KeywordTypeEnum.Category, "category1")

        // act
        Assertions.assertThat(keyword!!.value).isEqualTo("category1")
        val result = kwController.updateKeyword(keyword!!.id.value, ApiKeywordUpdate("updated Name","updated uri",KeywordTypeEnum.Category, "updated framework"), nullJwt)

        // assert
        Assertions.assertThat(result).isNotNull
        Assertions.assertThat(result).isExactlyInstanceOf(ResponseEntity::class.java)
        Assertions.assertThat(result.body).isExactlyInstanceOf(ApiKeyword::class.java)
        Assertions.assertThat(result.body!!.name).isEqualTo("updated Name")
        Assertions.assertThat(result.body!!.framework).isEqualTo("updated framework")
        Assertions.assertThat((result as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `searchRelatedSkills() should retrieve an existing RSD`() {
        // arrange
        val category1 = keywordRepository.create(KeywordTypeEnum.Category, "category1")
        val category2 = keywordRepository.create(KeywordTypeEnum.Category, "category2")

        richSkillRepository.create(
            user = "user1",
            updateObject = RsdUpdateObject(
                name = "skill-1",
                statement = "Skill 1",
                keywords = ListFieldUpdate(add = listOf(category1!!, category2!!)),
            ),
        )

        richSkillRepository.create(
            user = "user1",
            updateObject = RsdUpdateObject(
                name = "skill-2",
                statement = "Skill 2",
                keywords = ListFieldUpdate(add = listOf(category1!!)),
            ),
        )

        // act
        val result = kwController.searchKeywordSkills(
            uriComponentsBuilder = UriComponentsBuilder.newInstance(),
            id = category2.id.value,
            size = 10,
            from = 0,
            sort = SkillSortEnum.defaultSort.toString(),
            status = arrayOf(PublishStatus.Published.toString()),
        )

        // assert
        Assertions.assertThat(result).isNotNull
        Assertions.assertThat(result.body?.size == 1)
    }
}
