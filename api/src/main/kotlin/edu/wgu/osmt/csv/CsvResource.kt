package edu.wgu.osmt.csv

import com.opencsv.CSVWriter
import java.io.StringWriter
import java.io.Writer
import java.lang.RuntimeException

abstract class CsvResource<T>(val debugName: String) {

    /**
     * Defines the columns of this csv in their desired order.
     */
    abstract fun columnTranslations(): Array<CsvColumn<T>>

    /**
     * Override if opencsv defaults are not desired
     */
    open fun configureCsv(): CsvConfig = CsvConfig()

    /**
     * Produce a csv export from the list of data using this CsvResource's configuration and translations
     */
    fun toCsv(data: List<T>): String {
        // fail fast
        validate()

        val stringWriter = StringWriter()
        val csvWriter = getCsvWriter(stringWriter)

        writeHeaderRow(csvWriter)
        writeRows(data, csvWriter)

        return stringWriter.toString()
    }

    /*
     * Internal functions
     */

    private fun getCsvWriter(writer: Writer): CSVWriter {
        val config = configureCsv()

        return CSVWriter(writer,
                config.delimeter,
                config.quoteChar,
                config.escapeChar,
                config.lineEnd
        )
    }

    private fun validate() {
        if (configureCsv().includeHeader && columnTranslations().any { it.name.isEmpty() }) {
            throw CsvMissingHeaderException(debugName)
        }
    }

    private fun writeHeaderRow(csvWriter: CSVWriter) {
        if (configureCsv().includeHeader) {
            val headerRow = columnTranslations().map { column -> column.name }.toTypedArray()
            writeRow(headerRow, csvWriter)
        }
    }

    private fun writeRows(data: List<T>, csvWriter: CSVWriter) {
        val rowsList: List<Array<String>> = data.map { datum ->
            columnTranslations().map { it.translate(datum) }.toTypedArray()
        }
        rowsList.forEach { writeRow(it, csvWriter) }
    }

    private fun writeRow(rowData: Array<String>, csvWriter: CSVWriter) {
        csvWriter.writeNext(rowData)
    }
}


/**
 * Define the translation from the source object to a column's expected value
 */
data class CsvColumn<T>(
        val name: String = "",
        val translate: (T) -> String
)

/**
 * Configure the global attributes of a csv export
 */
data class CsvConfig(
        val delimeter: Char = CsvConfig.delimeter,
        val quoteChar: Char = CsvConfig.quoteChar,
        val escapeChar: Char = CsvConfig.escapeChar,
        val lineEnd: String = CsvConfig.lineEnd,
        val includeHeader: Boolean = CsvConfig.includeHeader
) {
    companion object Defaults { // Allows the default values to be shared with it's builder
        val delimeter: Char = CSVWriter.DEFAULT_SEPARATOR
        val quoteChar: Char = CSVWriter.DEFAULT_QUOTE_CHARACTER
        val escapeChar: Char = CSVWriter.DEFAULT_ESCAPE_CHARACTER
        val lineEnd: String = CSVWriter.DEFAULT_LINE_END
        val includeHeader: Boolean = true
    }
}

data class CsvMissingHeaderException(val debugName: String)
    : RuntimeException("Can't produce $debugName csv, one or more required column headers are missing.", null)