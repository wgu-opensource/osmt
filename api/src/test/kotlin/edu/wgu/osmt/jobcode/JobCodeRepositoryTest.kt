package edu.wgu.osmt.jobcode

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.richskill.RichSkillEsRepo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class JobCodeRepositoryTest @Autowired constructor(
    override val richSkillEsRepo: RichSkillEsRepo,
    override val collectionEsRepo: CollectionEsRepo,
    override val keywordEsRepo: KeywordEsRepo,
    override val jobCodeEsRepo: JobCodeEsRepo,
    val jobCodeRepository: JobCodeRepository
) : SpringTest(), BaseDockerizedTest, HasDatabaseReset, HasElasticsearchReset {

    @Test
    fun `Should find o*net jobcodes from detailed code`() {
        val noise = listOf("28-1100", "28-1100.00", "11-1234", "11-1234.56", "10-000", "29-1142.00", "29-1142.01")

        val jobCode1 = "29-1141.00"
        val jobCode2 = "29-1141.01"
        val jobCode3 = "29-1141.02"
        val detailedCode = "29-1141"

        (noise + listOf(jobCode1, jobCode2, jobCode3, detailedCode)).map {
            jobCodeRepository.create(
                it)
        }

        val result = jobCodeRepository.onetsByDetailCode(detailedCode).map { it.toModel() }

        assertThat(result.map{it.code}).containsAll(listOf(jobCode1, jobCode2, jobCode3))
        assertThat(result.count()).isEqualTo(3)
    }

    @Test
    fun `JobCode has children`() {
        val majorJobCode = jobCodeRepository.create("95-0000")
        jobCodeRepository.create("95-1000")
        jobCodeRepository.create("95-1100")
        val majorJobCodeHasChildren = jobCodeRepository.hasChildren(majorJobCode)
        assertThat(majorJobCodeHasChildren).isEqualTo(true)
    }

    @Test
    fun `Job code doesn't have children`() {
        val majorJobCode = jobCodeRepository.create("96-0000")
        val majorJobCodeHasChildren = jobCodeRepository.hasChildren(majorJobCode)
        assertThat(majorJobCodeHasChildren).isEqualTo(false)
    }

    @Test
    fun `Job code cannot be deleted`() {
        val majorJobCode = jobCodeRepository.create("97-0000")
        jobCodeRepository.create("97-1000")
        val apiBatchResult = jobCodeRepository.remove(majorJobCode.id.value)
        assertThat(apiBatchResult.success).isEqualTo(false)
        assertThat(apiBatchResult.totalCount).isEqualTo(0)
    }

    @Test
    fun `Job code can be deleted`() {
        val majorJobCode = jobCodeRepository.create("98-0000")
        val apiBatchResult = jobCodeRepository.remove(majorJobCode.id.value)
        assertThat(apiBatchResult.success).isEqualTo(true)
        assertThat(apiBatchResult.totalCount).isEqualTo(1)
    }
}
