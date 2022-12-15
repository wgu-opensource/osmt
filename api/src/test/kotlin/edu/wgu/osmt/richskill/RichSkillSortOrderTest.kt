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
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
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
    val nullJwt : Jwt? = null

    @BeforeAll
    fun setup() {
        mockData = MockData()
    }

    @Nested
    inner class SortedResults {
        @Test
        fun `sorted by default`() {
            //default sort is CategoryAsc

            // Arrange
            val size = 50
            val listOfSkills = mockData.getRichSkillDocs()
            richSkillEsRepo.saveAll(listOfSkills)

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
            val richSkillCategoriesASC: List<String?> = listOf(rsdList?.get(0)?.category, rsdList?.get(5)?.category)

            // Assert
//            assertTrue(Ordering.natural<String>().isOrdered(richSkillCategoriesASC))
        }
        @Test
        fun `sorted by category ASC and name ASC`() {
            // Arrange
            val size = 50
            val listOfSkills = mockData.getRichSkillDocs()
            richSkillEsRepo.saveAll(listOfSkills)

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
            val richSkillCategoriesASC: List<String?> = listOf(body?.get(0)?.category, body?.get(5)?.category)
            val richSkillNameASC: List<String?> = listOf(body?.get(0)?.name, body?.get(5)?.name)

            // Assert
//            assertTrue(Ordering.natural<String>().isOrdered(richSkillCategoriesASC))
//            assertTrue(Ordering.natural<String>().isOrdered(richSkillNameASC))
        }
        @Test
        fun `sorted by category DESC and name ASC`() {
            // Arrange
            val size = 50
            val listOfSkills = mockData.getRichSkillDocs()
            richSkillEsRepo.saveAll(listOfSkills)

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
            val richSkillCategoriesDESC: List<String?> = listOf(body?.get(0)?.category, body?.get(5)?.category)
            val richSkillNameASC: List<String?> = listOf(body?.get(0)?.name, body?.get(5)?.name)

//            println(Ordering.natural<String>().isOrdered(richSkillCategoriesDESC))
//
//            // Assert
//            assertFalse(Ordering.natural<String>().isOrdered(richSkillCategoriesDESC))
//            assertTrue(Ordering.natural<String>().isOrdered(richSkillNameASC))
        }
        @Test
        fun `sorted by name ASC`() {}
        @Test
        fun `sorted by name DESC`() {}
    }

//    fun verifySortOrder(skills: List<RichSkillDoc>?): Boolean {
//
//        return true
//    }
}
