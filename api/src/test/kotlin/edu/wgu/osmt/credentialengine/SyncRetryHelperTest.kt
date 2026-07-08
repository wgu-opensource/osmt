package edu.wgu.osmt.credentialengine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SyncRetryHelperTest {
    private val helper = SyncRetryHelper()

    @Test
    fun `succeeds on first try`() {
        var calls = 0
        val result =
            helper.withRetry(5, 10, 1.5) {
                calls++
                Result.success(42)
            }
        assertThat(result.getOrNull()).isEqualTo(42)
        assertThat(calls).isEqualTo(1)
    }

    @Test
    fun `retries then succeeds`() {
        var calls = 0
        val result: Result<String> =
            helper.withRetry(5, 5, 1.5) {
                calls++
                if (calls < 3) {
                    Result.failure(RuntimeException("fail"))
                } else {
                    Result.success("ok")
                }
            }
        assertThat(result.getOrNull()).isEqualTo("ok")
        assertThat(calls).isEqualTo(3)
    }

    @Test
    fun `exhausts retries and returns failure`() {
        var calls = 0
        val result: Result<Unit> =
            helper.withRetry(3, 5, 1.5) {
                calls++
                Result.failure(RuntimeException("persistent"))
            }
        assertThat(result.isFailure).isTrue
        assertThat(result.exceptionOrNull()?.message).isEqualTo("persistent")
        assertThat(calls).isEqualTo(3)
    }

    @Test
    fun `retry count capped at 10`() {
        var calls = 0
        helper.withRetry<Unit>(100, 1, 1.0) {
            calls++
            Result.failure(RuntimeException("x"))
        }
        assertThat(calls).isEqualTo(10)
    }
}
