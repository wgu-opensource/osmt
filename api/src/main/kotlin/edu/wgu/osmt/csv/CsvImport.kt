package edu.wgu.osmt.csv

import com.opencsv.bean.CsvToBeanBuilder
import edu.wgu.osmt.jobcode.JobCodeBreakout
import org.slf4j.Logger
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader

interface CsvImport<T> where T : CsvRow {
    val log: Logger

    val csvRowClass: Class<T>

    fun handleRows(rows: List<T>): Unit

    fun processCsv(csv_path: String) {
        log.info("Starting to process csv: ${csv_path}")

        var fileReader: BufferedReader? = null

        try {
            fileReader = BufferedReader(FileReader(csv_path))
            val csvToBean = CsvToBeanBuilder<T>(fileReader)
                .withType(csvRowClass)
                .withIgnoreLeadingWhiteSpace(true)
                .build()

            val rows = csvToBean.parse()
            handleRows(rows)
        } catch (e: FileNotFoundException) {
            log.error("Could not find file: ${csv_path}")
        } finally {
            fileReader?.close()
        }
    }
}

interface HasCodeHierarchy {
    var code: String?

    fun major(): String? {
        return code?.let { JobCodeBreakout.majorCode(it) }
    }

    fun minor(): String? {
        return code?.let { JobCodeBreakout.minorCode(it) }
    }

    fun broad(): String? {
        return code?.let { JobCodeBreakout.broadCode(it) }
    }

    fun detailed(): String? {
        return code?.let { JobCodeBreakout.detailedCode(it) }
    }
}

interface CsvRow
