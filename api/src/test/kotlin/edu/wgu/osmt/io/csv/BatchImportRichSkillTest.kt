package edu.wgu.osmt.io.csv

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.mockdata.MockData
import edu.wgu.osmt.richskill.RichSkillEsRepo
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
internal class BatchImportRichSkillTest @Autowired constructor(
        override val richSkillEsRepo: RichSkillEsRepo,
        override val collectionEsRepo: CollectionEsRepo,
        override val keywordEsRepo: KeywordEsRepo,
        override val jobCodeEsRepo: JobCodeEsRepo
) : SpringTest(), BaseDockerizedTest, HasDatabaseReset, HasElasticsearchReset {

    @Autowired
    lateinit var batchImportRichSkill: BatchImportRichSkill

    @Test
    fun testHandleRows() {
        // Arrange
        val mockData = MockData()
        val numOfSkills = 3
        val richSkillRows = mockData.getRichSkillRows()
        val listOfRichSkillRows = mutableListOf<RichSkillRow>()

        for (i in 1..numOfSkills ) {
            listOfRichSkillRows.add(richSkillRows[i])
        }

        // Act
        batchImportRichSkill.handleRows(listOfRichSkillRows)

        val skillResult = richSkillEsRepo.byApiSearch(ApiSearch())

        // Assert
        Assertions.assertThat(skillResult.searchHits.map { it.content.name }).contains(richSkillRows[1].skillName)
        Assertions.assertThat(skillResult.searchHits.size).isEqualTo(numOfSkills)

    }
}