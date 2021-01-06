package edu.wgu.osmt.jobcode

import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.TestObjectHelpers
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.richskill.RichSkillEsRepo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset

@Transactional
class JobCodeEsRepoTest @Autowired constructor(
    override val richSkillEsRepo: RichSkillEsRepo,
    override val collectionEsRepo: CollectionEsRepo,
    override val keywordEsRepo: KeywordEsRepo,
    override val jobCodeEsRepo: JobCodeEsRepo
) : SpringTest(), HasDatabaseReset, HasElasticsearchReset {


    @Test
    fun `Should get results for type ahead searches`(){
        val noiseJobCodes = (1..50).map{
            val codeString = if (it < 10){"50-500${it}"} else {"50-50${it}"}
            TestObjectHelpers.randomJobCode().copy(code = codeString)
        }.also{
            jobCodeEsRepo.saveAll(it)
        }
        val testJobCodes = listOf(
            TestObjectHelpers.randomJobCode().copy(code = "29-2071.00"),
            TestObjectHelpers.randomJobCode().copy(code = "29-2070.00"),
            TestObjectHelpers.randomJobCode().copy(code = "29-2000.00"),
            JobCode(id = TestObjectHelpers.elasticIdCounter, code = "29-0000.00", name = null, creationDate = LocalDateTime.now(ZoneOffset.UTC)) // should be ignored in results
        )
        jobCodeEsRepo.saveAll(testJobCodes)

        val prefixSearch = jobCodeEsRepo.typeAheadSearch("29-")
        val prefixSearch2 = jobCodeEsRepo.typeAheadSearch("29-2")
        val prefixSearch3 = jobCodeEsRepo.typeAheadSearch("29-207")
        val matchSearch = jobCodeEsRepo.typeAheadSearch("29-2071.00")

        assertThat(prefixSearch.searchHits.count()).isEqualTo(3)
        assertThat(prefixSearch2.searchHits.count()).isEqualTo(3)
        assertThat(prefixSearch3.searchHits.count()).isEqualTo(2)
        assertThat(matchSearch.searchHits.count()).isEqualTo(1)
        assertThat(matchSearch.searchHits.first().content.code).isEqualTo("29-2071.00")
    }
}
