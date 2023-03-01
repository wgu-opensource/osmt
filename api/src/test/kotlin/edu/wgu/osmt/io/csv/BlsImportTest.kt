package edu.wgu.osmt.io.csv

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.elasticsearch.SearchController
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.mockdata.MockData
import edu.wgu.osmt.richskill.RichSkillEsRepo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder


@Transactional
internal class BlsImportTest @Autowired constructor(
        override val richSkillEsRepo: RichSkillEsRepo,
        override val collectionEsRepo: CollectionEsRepo,
        override val keywordEsRepo: KeywordEsRepo,
        override val jobCodeEsRepo: JobCodeEsRepo
): SpringTest(), BaseDockerizedTest, HasDatabaseReset, HasElasticsearchReset {

    @Autowired
    lateinit var blsImport: BlsImport

    @Autowired
    lateinit var searchController: SearchController

    @Test
    fun testHandleRows() {
        // Arrange
        val listOfBlsJobCodes = MockData().getBlsJobCodes()

        // Act
        blsImport.handleRows(listOfBlsJobCodes)

        // this major code should pull back many related minor, borad, and detailed job codes
        val expectedMajorCode = "15-0000"
        // searchJobCodes will only return 10 items in the list
        val result = expectedMajorCode?.let { searchController.searchJobCodes(UriComponentsBuilder.newInstance(), it) }
        val listOfApiJobCodes = result?.body?.map { it.code }

        // Assert
        assertThat(result).isNotNull
        assertThat(listOfApiJobCodes).contains(expectedMajorCode)
    }
}
