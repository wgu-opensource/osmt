package edu.wgu.osmt.jobcode

import edu.wgu.osmt.api.model.ApiJobCode
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class JobCodeTest {

    @Test
    fun create() {
        // Arrange
        val code = "someCode"

        // Act
        val dup = JobCode.create(code)

        // Assert
        Assertions.assertThat(dup.code).isEqualTo(code)
        Assertions.assertThat(dup.creationDate).isNotNull()
    }

    companion object {
        fun assertEquals(jobCode: JobCode, apiJobCode: ApiJobCode) {
            Assertions.assertThat(apiJobCode.code).isEqualTo(jobCode.code)
            Assertions.assertThat(apiJobCode.name).isEqualTo(jobCode.name)
            Assertions.assertThat(apiJobCode.framework).isEqualTo(jobCode.framework)
        }
    }
}