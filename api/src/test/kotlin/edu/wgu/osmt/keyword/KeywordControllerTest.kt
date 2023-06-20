package edu.wgu.osmt.keyword

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
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
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.util.ReflectionTestUtils
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


    @BeforeAll
    fun setup() {
        mockData = MockData()
    }

    @Test
    fun `allCategoriesPaginated() should retrieve an existing category`() {
        // arrange
        val size: Int = 2
        val from: Int = 2
        val sort: KeywordSortEnum = KeywordSortEnum.SkillCountDesc

        mockData.getKeywords().map {
            keywordRepository.create(it.type, it.value, it.uri, it.framework)
        }

        // act
        val result = kwController.allCategoriesPaginated(
            uriComponentsBuilder = UriComponentsBuilder.newInstance(),
            size = size,
            from = from,
            sort = sort.toString(),
        )

        // assert
        Assertions.assertThat(result).isNotNull
        Assertions.assertThat(result.body?.size).isEqualTo(size)
    }

    @Test
    fun `categoryById() should retrieve an existing category`() {
        // arrange
        val keyword = keywordRepository.create(KeywordTypeEnum.Category, "category1")

        // act
        val result = kwController.categoryById(keyword?.id?.value.toString())

        // assert
        Assertions.assertThat(result).isNotNull
    }

    @Test
    fun `getCategorySkills() should retrieve an existing category`() {
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
        val result = kwController.getCategorySkills(
            uriComponentsBuilder = UriComponentsBuilder.newInstance(),
            identifier = category2.id.toString(),
            size = 10,
            from = 0,
            sort = SkillSortEnum.defaultSort.toString(),
            status = arrayOf(PublishStatus.Published.toString()),
        )

        // assert
        Assertions.assertThat(result).isNotNull
        Assertions.assertThat(result.body?.size == 1)
    }

    @Test
    fun `searchCategorySkills() should retrieve an existing category`() {
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
        val result = kwController.searchCategorySkills(
            uriComponentsBuilder = UriComponentsBuilder.newInstance(),
            identifier = category2.id.toString(),
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
