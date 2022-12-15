package edu.wgu.osmt.richskill

//import com.google.common.collect.Ordering
import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.csv.BatchImportRichSkill
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.mockdata.MockData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder


@Transactional
internal class RichSkillSortOrderTest @Autowired constructor(
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

    private val nullJwt : Jwt? = null

    private lateinit var listOfSkills : List<RichSkillDoc>

    private var size : Int = 0

    @BeforeAll
    fun setup() {
        mockData = MockData()
        listOfSkills = mockData.getRichSkillDocs()
        richSkillEsRepo.saveAll(listOfSkills)
        size = 50
    }

    @Nested
    inner class SortedResults {
        @Test
        fun `sorted by default (cat desc)`() {
            // Act
            val result = richSkillController.allPaginated(
                    UriComponentsBuilder.newInstance(),
                    size,
                    0,
                    arrayOf("draft", "published"),
                    "",
                    nullJwt
            )
            val rsdList: List<RichSkillDoc>? = result.body

            // Assert
            assertThat(rsdList).isSortedAccordingTo(
                Comparator.comparing { r: RichSkillDoc -> r.category!! }
            )
        }
        @Test
        fun `sorted by category ASC and name ASC`() {
            // Act
            val result = richSkillController.allPaginated(
                    UriComponentsBuilder.newInstance(),
                    size,
                    0,
                    arrayOf("draft", "published"),
                    "name.asc",
                    nullJwt
            )
            val body: List<RichSkillDoc>? = result.body
            val byNameAndCategory = Comparator.comparing { r: RichSkillDoc -> r.category!! }
                .thenComparing { r -> r.name!! }

            // Assert
            assertThat(body).isSortedAccordingTo(byNameAndCategory)

        }
        @Test
        fun `sorted by category DESC and name ASC`() {
            // Act
            val result = richSkillController.allPaginated(
                    UriComponentsBuilder.newInstance(),
                    size,
                    0,
                    arrayOf("draft", "published"),
                    "name.desc",
                    nullJwt
            )
            val body: List<RichSkillDoc>? = result.body
            val byCategoryDescAndThenByName = Comparator.comparing { r: RichSkillDoc -> r.category!! }.reversed()
                .thenComparing { r -> r.name!! }

            // Assert
            assertThat(body).isSortedAccordingTo(byCategoryDescAndThenByName)
        }
        @Test
        fun `sorted by name ASC`() {
            // Act
            val result = richSkillController.allPaginated(
                UriComponentsBuilder.newInstance(),
                size,
                0,
                arrayOf("draft", "published"),
                "name.asc",
                nullJwt
            )
            val rsdList: List<RichSkillDoc>? = result.body

            // Assert
            assertThat(rsdList).isSortedAccordingTo(
                Comparator.comparing { r: RichSkillDoc -> r.name!! }
            )
        }
        @Test
        fun `sorted by name DESC`() {
            // Act
            val result = richSkillController.allPaginated(
                UriComponentsBuilder.newInstance(),
                size,
                0,
                arrayOf("draft", "published"),
                "name.asc",
                nullJwt
            )
            val rsdList: List<RichSkillDoc>? = result.body

            // Assert
            assertThat(rsdList).isSortedAccordingTo(
                Comparator.comparing { r: RichSkillDoc -> r.name!! }.reversed()
            )
        }
    }
}
