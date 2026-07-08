package edu.wgu.osmt.credentialengine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.RepeatedTest

class CorrelationIdTest {
    @RepeatedTest(5)
    fun `generates alphanumeric id of length 10`() {
        val id = generateCorrelationId()
        assertThat(id).hasSize(10)
        assertThat(id).matches("[a-z0-9]+")
    }

    @RepeatedTest(3)
    fun `ids are likely unique`() {
        val a = generateCorrelationId()
        val b = generateCorrelationId()
        assertThat(a).isNotEqualTo(b)
    }
}
