package edu.wgu.osmt.richskill

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.config.NAME_ASC
import edu.wgu.osmt.config.NAME_DESC
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.mockdata.MockData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder
import java.lang.String.CASE_INSENSITIVE_ORDER


@Transactional
internal class RichSkillSortOrderTest @Autowired constructor(
        override val richSkillEsRepo: RichSkillEsRepo,
        override val collectionEsRepo: CollectionEsRepo,
        override val keywordEsRepo: KeywordEsRepo,
        override val jobCodeEsRepo: JobCodeEsRepo
): SpringTest(), BaseDockerizedTest, HasDatabaseReset, HasElasticsearchReset {

    @Autowired
    lateinit var richSkillController: RichSkillController

    private lateinit var mockData : MockData

    private val nullJwt : Jwt? = null



    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class SortedResults {

        var size: Int = 0
        private lateinit var listOfSkills: List<RichSkillDoc>

        @BeforeAll
        fun setup() {
            // Arrange
            mockData = MockData()
            size = 50
            listOfSkills = mockData.getRichSkillDocs()
            richSkillEsRepo.saveAll(listOfSkills)
        }

        @Test
        fun `sorted by default(name ASC)`() {
            // Act
            val result = richSkillController.allPaginatedV2(
                    UriComponentsBuilder.newInstance(),
                    size,
                    0,
                    arrayOf("draft", "published"),
                    "",
                    nullJwt
            )
            val rsdList: List<RichSkillDocV2>? = result.body

            // Assert
            assertThat(rsdList).isSortedAccordingTo(
                Comparator.comparing(RichSkillDocV2::name, CASE_INSENSITIVE_ORDER)
            )
        }
        @Test
        fun `sorted by name ASC`() {
            // Act
            val result = richSkillController.allPaginatedV2(
                UriComponentsBuilder.newInstance(),
                size,
                0,
                arrayOf("draft", "published"),
                NAME_ASC,
                nullJwt
            )
            val rsdList: List<RichSkillDocV2>? = result.body

            // Assert
            assertThat(rsdList).isSortedAccordingTo(
                Comparator.comparing(RichSkillDocV2::name, CASE_INSENSITIVE_ORDER)
            )
        }
        @Test
        fun `sorted by name DESC`() {
            // Act
            val result = richSkillController.allPaginatedV2(
                UriComponentsBuilder.newInstance(),
                size,
                0,
                arrayOf("draft", "published"),
                NAME_DESC,
                nullJwt
            )
            val rsdList: List<RichSkillDocV2>? = result.body

            // Assert
            assertThat(rsdList).isSortedAccordingTo(
                Comparator.comparing(RichSkillDocV2::name, CASE_INSENSITIVE_ORDER).reversed()
            )
        }
    }
}
