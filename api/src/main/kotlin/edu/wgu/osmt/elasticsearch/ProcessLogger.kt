package edu.wgu.osmt.elasticsearch

import org.slf4j.Logger
import java.lang.Long.min

internal class ProcessLogger(
    private val name: String,
    logger: Logger
) {
    private val logger: Logger = logger
    private val start: Long = System.currentTimeMillis()
    private var totalCount = 0L
    private var count: Long = 0

    fun increment(vararg args: String?) {
        count++
        if (logger.isTraceEnabled) {
            logger.trace(
                String.format(
                    "Re-indexing %s (%.1f%%): %s",
                    name,
                    ((count).toFloat() * 100.0 / totalCount).toFloat(),
                    if (args.size > 1 && args[1] != null) String.format(
                        "%s (\"%s\")", args[0],
                        args[1]
                    ) else args[0]
                )
            )
        }
    }

    fun offset() : Long {
        return count
    }

    fun start(totalCount: Long, count: Long, limit: Int) {
        this.totalCount = totalCount
        this.count = count
        if (logger.isDebugEnabled) {
            logger.debug("Re-indexing {} records ({}-{}) for {}", totalCount, count+1, min(totalCount, count + limit), name)
        } else {
            if (this.count == 0L) {
                logger.info("Re-indexing {} records for {}", totalCount, name)
            }
        }
    }

    fun finish() {
        val totalTime = System.currentTimeMillis() - start
        logger.info("Re-indexed {} {} ({}.{}s)", count, name, totalTime / 1000, totalTime % 1000)
    }

}