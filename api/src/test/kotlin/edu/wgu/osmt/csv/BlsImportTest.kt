package edu.wgu.osmt.csv

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

    fun <T> concatenate(vararg lists: List<T>?): List<T>? {
        val flat = lists.filterNotNull().flatten()
        return when {
            flat.isNotEmpty() -> flat
            else -> null
        }
    }

    @Test
    fun testHandleRows() {
        // Arrange
        val mockData = MockData()
        val listOfBlsJobCodes = mockData.getBlsJobCodes()

        // Act
        blsImport.handleRows(listOfBlsJobCodes)
        val result = listOfBlsJobCodes[0].code?.let { searchController.searchJobCodes(UriComponentsBuilder.newInstance(), it) }

//        val listOfJobCodes = concatenate(result?.body?.map { it.jobRoleCode },
//                result?.body?.map { it.majorCode },
//                result?.body?.map { it.minorCode },
//                result?.body?.map { it.broadCode },
//                result?.body?.map { it.detailedCode })
//
//        // Assert
//        assertThat(result).isNotNull
//        assertThat(listOfJobCodes).contains(listOfBlsJobCodes[0].code)
    }
}