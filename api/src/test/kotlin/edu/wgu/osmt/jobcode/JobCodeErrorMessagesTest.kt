package edu.wgu.osmt.jobcode

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class JobCodeErrorMessagesTest {

    @Test
    fun `Error should be doesn't exist`() {
        val hasChildren = false
        val hasRSDs = false
        Assertions.assertThat(JobCodeErrorMessages.forDeleteError(hasChildren, hasRSDs)).isEqualTo(JobCodeErrorMessages.JobCodeNotExist.apiValue)
    }

    @Test
    fun `Error should be job code has children`() {
        val hasChildren = true
        val hasRSDs = false
        Assertions.assertThat(JobCodeErrorMessages.forDeleteError(hasChildren, hasRSDs)).isEqualTo(JobCodeErrorMessages.JobCodeHasChildren.apiValue)
    }

    @Test
    fun `Error should be job code has RSDs`() {
        val hasChildren = false
        val hasRSDs = true
        Assertions.assertThat(JobCodeErrorMessages.forDeleteError(hasChildren, hasRSDs)).isEqualTo(JobCodeErrorMessages.JobCodeHasRSD.apiValue)
    }

}
