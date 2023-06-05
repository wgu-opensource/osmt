package edu.wgu.osmt.richskill

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.jobcode.JobCodeRepository
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class RichSkillJobCodeRepositoryTest  @Autowired constructor(
    val richSkillRepository: RichSkillRepository,
    val richSkillJobCodeRepository: RichSkillJobCodeRepository,
    val jobCodeRepository: JobCodeRepository
) : SpringTest(), BaseDockerizedTest, HasDatabaseReset {


    val userString = "unittestuser"

    @Test
    fun `The job code should have RSDs`() {
        val jobCodeDao = jobCodeRepository.create("95-0000")
        richSkillRepository.create(
            RsdUpdateObject(statement = "This is an statement", name = "Test 20230506", jobCodes = ListFieldUpdate(add = listOf(jobCodeDao))),
            userString
        )
        assertThat(richSkillJobCodeRepository.hasRSDs(jobCodeDao)).isEqualTo(true)
    }

    @Test
    fun `The job code should not have RSDs`() {
        val jobCodeDao = jobCodeRepository.create("96-0000")
        assertThat(richSkillJobCodeRepository.hasRSDs(jobCodeDao)).isEqualTo(false)
    }

}
