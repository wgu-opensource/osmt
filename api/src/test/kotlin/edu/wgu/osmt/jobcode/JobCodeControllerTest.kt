package edu.wgu.osmt.jobcode

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.api.model.JobCodeUpdate
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset

@Transactional
internal class JobCodeControllerTest @Autowired constructor(
    val jobCodeEsRepo: JobCodeEsRepo,
    val jobCodeRepository: JobCodeRepository
) : SpringTest(), BaseDockerizedTest, HasDatabaseReset {

    @Autowired
    lateinit var jobCodeController: JobCodeController
    val dao = JobCodeDao.Companion

    @Test
    fun `Index should return an array with almost one job code`() {
        dao.new {
            this.code = "code"
            this.framework = "framework"
            this.name = "targetNodeName"
            this.creationDate = LocalDateTime.now(ZoneOffset.UTC)
            this.name = "my name"
            this.major = "my major"
        }.also { jobCodeEsRepo.save(it.toModel()) }
        val result = jobCodeController.allPaginated(50, 0, "name.asc", "name")
        Assertions.assertThat(result.body).hasSizeGreaterThan(0)
    }

    @Test
    fun `By id should find a job code`() {
        val daoJobCode = dao.new {
            this.code = "code"
            this.framework = "framework"
            this.name = "targetNodeName"
            this.creationDate = LocalDateTime.now(ZoneOffset.UTC)
            this.name = "my name"
            this.major = "my major"
        }
        val esJobCode = jobCodeEsRepo.save(jobCodeEsRepo.save(daoJobCode.toModel()))
        val result = esJobCode.id?.let { jobCodeController.byId(it) }
        if (result != null) {
            Assertions.assertThat(result.body).isNotNull
        }
        Assertions.assertThat((result as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Create should return created job codes`() {
        val result = jobCodeController.createJobCode(
            listOf(JobCodeUpdate("my code", "my framework"))
        )
        Assertions.assertThat(result.body).hasSizeGreaterThan(0)
    }

    @Test
    fun `Update should return job codes with updated properties`() {
        val result = jobCodeController.updateJobCode(
            1,
            JobCodeUpdate("my code", "my framework")
        )
        Assertions.assertThat(result).isNotNull
        Assertions.assertThat((result as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Delete should return status 200`() {
        val result = jobCodeController.deleteJobCode(
            1
        )
        Assertions.assertThat(result).isNotNull
        Assertions.assertThat((result as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
    }


}
