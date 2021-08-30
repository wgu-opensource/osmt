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
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder


@Transactional
internal class OnetImportTest @Autowired constructor(
        override val richSkillEsRepo: RichSkillEsRepo,
        override val collectionEsRepo: CollectionEsRepo,
        override val keywordEsRepo: KeywordEsRepo,
        override val jobCodeEsRepo: JobCodeEsRepo
): SpringTest(), BaseDockerizedTest, HasDatabaseReset, HasElasticsearchReset {

    @Autowired
    lateinit var onetImport: OnetImport

    @Autowired
    lateinit var searchController: SearchController

    private lateinit var mockData : MockData

    @BeforeAll
    fun setup() {
        mockData = MockData()
    }

    @Test
    fun testHandleRows(){
        // Arrange
        val listOfOnetCodes = mockData.getOnetJobCodes()

        // Act
        onetImport.handleRows(listOfOnetCodes)
        val result = listOfOnetCodes[0].code?.let { searchController.searchJobCodes(UriComponentsBuilder.newInstance(), it) }


        // Assert
        assertThat(result).isNotNull
        assertThat(result?.body?.map { it.jobRoleCode }).contains(listOfOnetCodes[0].code)
    }

}
