package edu.wgu.osmt.csv

import com.opencsv.CSVWriter
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.StringWriter
import java.io.Writer
import java.util.stream.Stream
import kotlin.streams.toList


abstract class CsvResource<T>(val debugName: String) {

    /**
     * Defines the columns of this csv in their desired order.
     */
    abstract fun columnTranslations(data: List<T>): Array<CsvColumn<T>>

    /**
     * Override if opencsv defaults are not desired
     */
    open fun configureCsv(): CsvConfig = CsvConfig()

    /**
     * Produce a csv export from the list of data using this CsvResource's configuration and translations
     */
    fun toCsv(data: List<T>): String {
        // fail fast
        validate(data)

        val stringWriter = StringWriter()
        val csvWriter = getCsvWriter(stringWriter)

        writeHeaderRow(data, csvWriter)
        writeRows(data, csvWriter)

        return stringWriter.toString()
    }

    fun writeCsvToOutputStream(response: OutputStream, data : Stream<T>) {

        val csvList = toCsv(data.toList().take(12000))
        try {
            BufferedOutputStream(response).use { writer ->
                csvList.forEach { rsd ->
                    try {
                        writer.write(rsd.code)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
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

    private fun validate(data: List<T>) {
        if (configureCsv().includeHeader && columnTranslations(data).any { it.name.isEmpty() }) {
            throw CsvMissingHeaderException(debugName)
        }
    }

    private fun writeHeaderRow(data: List<T>, csvWriter: CSVWriter) {
        if (configureCsv().includeHeader) {
            val headerRow = columnTranslations(data).map { column -> column.name }.toTypedArray()
            writeRow(headerRow, csvWriter)
        }
    }

    private fun writeRows(data: List<T>, csvWriter: CSVWriter) {
        val rowsList: List<Array<String>> = data.map { datum ->
            columnTranslations(data).map { it.translate(datum) }.toTypedArray()
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