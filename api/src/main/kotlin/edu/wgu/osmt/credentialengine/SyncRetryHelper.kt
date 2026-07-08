package edu.wgu.osmt.credentialengine

import org.springframework.stereotype.Component
import kotlin.math.min
import kotlin.math.pow

/** Fail-safe caps to avoid runaway retries. */
private const val MAX_RETRY_ATTEMPTS = 10
private const val MAX_DELAY_MS = 60_000L

@Component
class SyncRetryHelper {
    /**
     * Executes the block with retries. Exponential backoff: initialDelayMs * multiplier^attempt.
     * Caps: retry attempts at 10, delay at 60 seconds.
     */
    fun <T> withRetry(
        attempts: Int,
        initialDelayMs: Long,
        delayMultiplier: Double,
        block: () -> Result<T>,
    ): Result<T> {
        val safeAttempts = min(attempts, MAX_RETRY_ATTEMPTS)
        var lastFailure: Throwable? = null
        repeat(safeAttempts) { attempt ->
            val result = block()
            if (result.isSuccess) return result
            lastFailure = result.exceptionOrNull()
            if (attempt < safeAttempts - 1) {
                val delayMs = computeDelay(attempt, initialDelayMs, delayMultiplier)
                Thread.sleep(delayMs)
            }
        }
        return Result.failure(
            lastFailure ?: IllegalStateException("Retry exhausted with no exception"),
        )
    }

    private fun computeDelay(
        attempt: Int,
        initialDelayMs: Long,
        multiplier: Double,
    ): Long {
        val raw = initialDelayMs * multiplier.pow(attempt)
        return min(raw.toLong(), MAX_DELAY_MS)
    }
}
