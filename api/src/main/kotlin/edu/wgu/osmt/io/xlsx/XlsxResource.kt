package edu.wgu.osmt.io.xlsx

import com.opencsv.CSVWriter
import edu.wgu.osmt.io.common.TabColumn
import edu.wgu.osmt.io.common.TabularResource
import edu.wgu.osmt.richskill.RichSkillAndCollections
import org.apache.poi.ss.usermodel.BuiltinFormats
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream

abstract class XlsxResource<T>(val debugName: String) : TabularResource<XlsxColumn<T>, T> {
    private val workbook: XSSFWorkbook = XSSFWorkbook()
    private var rowCount: Int = 0

    /**
     * Defines the columns of this xlsx in their desired order.
     */
    abstract override fun columnTranslations(data: List<T>): Array<XlsxColumn<T>>

    /**
     * Produce a xlsx export from the list of data using this XlsxResource's configuration and translations
     */
    fun toXlsx(data: List<T>): ByteArray {
        val sheet: XSSFSheet = this.workbook.createSheet()

        writeHeaderRow(data, sheet)
        writeRows(data, sheet)

        val output: ByteArrayOutputStream = ByteArrayOutputStream()
        workbook.write(output)

        return output.toByteArray()
    }

    private fun writeHeaderRow(data: List<T>, sheet: XSSFSheet) {
        if (true) {
            val headerRow = columnTranslations(data).map { column -> column.name }.toTypedArray()
            writeRow(headerRow, sheet, true)
        }
    }

    private fun writeRows(data: List<T>, sheet: XSSFSheet) {
        val rowsList: List<Array<String>> = data.map { datum ->
            columnTranslations(data).map { it.translate(datum) }.toTypedArray()
        }
        rowsList.forEach { writeRow(it, sheet) }
    }

    private fun writeRow(rowData: Array<String>, sheet: XSSFSheet, isHeader: Boolean=false) {
        val row: Row = sheet.createRow(this.rowCount)

        rowData.forEachIndexed { colIndex, element ->
            if (isHeader) {
                val format = BuiltinFormats.getBuiltinFormat("@")
                val textStyle = this.workbook.createCellStyle()
                textStyle.setDataFormat(format)
                sheet.setDefaultColumnStyle(colIndex, textStyle)
            }

            val cell: Cell = row.createCell(colIndex)
            cell.setCellValue(element)
        }

        this.rowCount++
    }
}

data class XlsxColumn<T>(
    val name: String = "",
    val translate: (T) -> String
): TabColumn<T>