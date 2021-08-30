package edu.wgu.osmt.elasticsearch

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test

internal class ProcessLoggerTest {

    @Test
    fun testProcessLoggerSplitIntoBatches() {
        // Arrange
        val numBatches = 2L
        val batchSize = 5
        val logger = TestLogger("trace")
        val trace = ProcessLogger("ProcessLoggerTest", logger)

        // Act
        for (batch in 1..numBatches) {
            trace.start(numBatches*batchSize, (batch-1)*batchSize, batchSize )
            for (i in 1..batchSize) {
                trace.increment("test")
            }
        }
        trace.finish()

        // Assert
        assertThat(logger.log.size).isEqualTo(13)
        assertThat(logger.log[0]).isEqualTo("debug: Message: Re-indexing 10 records (1-5) for ProcessLoggerTest")
        assertThat(logger.log[1]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (10.0%): test")
        assertThat(logger.log[2]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (20.0%): test")
        assertThat(logger.log[3]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (30.0%): test")
        assertThat(logger.log[4]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (40.0%): test")
        assertThat(logger.log[5]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (50.0%): test")
        assertThat(logger.log[6]).isEqualTo("debug: Message: Re-indexing 10 records (6-10) for ProcessLoggerTest")
        assertThat(logger.log[7]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (60.0%): test")
        assertThat(logger.log[8]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (70.0%): test")
        assertThat(logger.log[9]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (80.0%): test")
        assertThat(logger.log[10]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (90.0%): test")
        assertThat(logger.log[11]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (100.0%): test")
        assertThat(logger.log[12]).startsWith("info: Message: Re-indexed 10 ProcessLoggerTest")
    }

    @Test
    fun testProcessLoggerSingleBatchInfoLevel() {
        // Arrange
        val numRecords = 10
        val logger = TestLogger("info")
        val trace = ProcessLogger("ProcessLoggerTest", logger)

        // Act
        trace.start(numRecords.toLong(), 0, numRecords )
        for (i in 1..numRecords) {
            trace.increment("arg1")
            assertThat(trace.offset()).isEqualTo(i.toLong())
        }
        trace.finish()

        // Assert
        assertThat(logger.log.size).isEqualTo(2)
        assertThat(logger.log[0]).isEqualTo("info: Message: Re-indexing 10 records for ProcessLoggerTest")
        assertThat(logger.log[1]).startsWith("info: Message: Re-indexed 10 ProcessLoggerTest")
    }


    @Test
    fun testProcessLoggerSingleBatchTraceLevel() {
        // Arrange
        val numRecords = 10
        val logger = TestLogger("trace")
        val trace = ProcessLogger("ProcessLoggerTest", logger)

        // Act
        trace.start(numRecords.toLong(), 0, numRecords )
        for (i in 1..numRecords) {
            trace.increment("arg1", "arg2")
            assertThat(trace.offset()).isEqualTo(i.toLong())
        }
        trace.finish()

        // Assert
        assertThat(logger.log.size).isEqualTo(12)
        assertThat(logger.log[0]).isEqualTo("debug: Message: Re-indexing 10 records (1-10) for ProcessLoggerTest")
        assertThat(logger.log[1]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (10.0%): arg1 (\"arg2\")")
        assertThat(logger.log[2]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (20.0%): arg1 (\"arg2\")")
        assertThat(logger.log[3]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (30.0%): arg1 (\"arg2\")")
        assertThat(logger.log[4]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (40.0%): arg1 (\"arg2\")")
        assertThat(logger.log[5]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (50.0%): arg1 (\"arg2\")")
        assertThat(logger.log[6]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (60.0%): arg1 (\"arg2\")")
        assertThat(logger.log[7]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (70.0%): arg1 (\"arg2\")")
        assertThat(logger.log[8]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (80.0%): arg1 (\"arg2\")")
        assertThat(logger.log[9]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (90.0%): arg1 (\"arg2\")")
        assertThat(logger.log[10]).isEqualTo("trace: Message: Re-indexing ProcessLoggerTest (100.0%): arg1 (\"arg2\")")
        assertThat(logger.log[11]).startsWith("info: Message: Re-indexed 10 ProcessLoggerTest")
    }
}
