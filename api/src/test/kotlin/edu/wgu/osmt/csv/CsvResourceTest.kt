package edu.wgu.osmt.csv

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

data class TestCsvRow(val id: Int, val leftValue: String, val rightValue: String)

internal class CsvResourceTest {

    @Test
    fun goldenPathTest() {
        // Setup
        val expectedCsv = "\"left\",\"middle\",\"right\"\n" +
                "\"1\",\"a\",\"one\"\n" +
                "\"2\",\"b\",\"two\"\n" +
                "\"3\",\"c\",\"three\"\n" +
                "\"4\",\"d\",\"four\"\n" +
                "\"5\",\"e\",\"five\"\n"


        val data: List<TestCsvRow> = listOf(
                TestCsvRow(1, "a","one"),
                TestCsvRow(2, "b", "two"),
                TestCsvRow(3, "c", "three"),
                TestCsvRow(4, "d", "four"),
                TestCsvRow(5, "e", "five")
        )

        val columnDefinitions: Array<CsvColumn<TestCsvRow>> = arrayOf(
                CsvColumn("left") { it.id.toString() },
                CsvColumn("middle") { it.leftValue },
                CsvColumn("right") { it.rightValue }
        )

        val exporter = getCsvTestResource(columnDefinitions = columnDefinitions)

        // Execute
        val csv = exporter.toCsv(data)

        // Verify
        assert(csv == expectedCsv) {
            "The generated csv did not match.\n\texpected=${expectedCsv}\n\tactual=${csv}"
        }
    }

    @Test
    fun withIgnoredFieldTest() {
        // Setup
        val expectedCsv = "\"left\",\"right\"\n" +
                "\"1\",\"one\"\n" +
                "\"2\",\"two\"\n"

        val data: List<TestCsvRow> = listOf(
                TestCsvRow(1, "a","one"),
                TestCsvRow(2, "b", "two")
        )

        val columnDefinitions: Array<CsvColumn<TestCsvRow>> = arrayOf(
                CsvColumn("left") { it.id.toString() },
                CsvColumn("right") { it.rightValue }
        )

        val exporter = getCsvTestResource(columnDefinitions = columnDefinitions)

        // Execute
        val csv = exporter.toCsv(data)

        // Verify
        assert(csv == expectedCsv) {
            "The generated csv did not match.\n\texpected=${expectedCsv}\n\tactual=${csv}"
        }
    }

    @Test
    fun withNoHeaders() {
        // Setup
        val expectedCsv = "\"1\",\"a\",\"one\"\n" +
                "\"2\",\"b\",\"two\"\n" +
                "\"3\",\"c\",\"three\"\n"
        val data: List<TestCsvRow> = listOf(
                TestCsvRow(1, "a","one"),
                TestCsvRow(2, "b", "two"),
                TestCsvRow(3, "c", "three")
        )

        val columnDefinitions: Array<CsvColumn<TestCsvRow>> = arrayOf(
                CsvColumn("left") { it.id.toString() },
                CsvColumn("middle") { it.leftValue },
                CsvColumn("right") { it.rightValue }
        )

        val exporter = getCsvTestResource(
                columnDefinitions = columnDefinitions,
                configuration = CsvConfig(includeHeader = false)
        )

        // Execute
        val csv = exporter.toCsv(data)

        // Verify
        assert(csv == expectedCsv) {
            "The generated csv did not match.\n\texpected=${expectedCsv}\n\tactual=${csv}"
        }
    }

    @Test
    fun DSLGoldenPathTest() {
        // Setup
        val expectedCsv = "\"1\",\"a\",\"one\"\n" +
                "\"2\",\"b\",\"two\"\n" +
                "\"3\",\"c\",\"three\"\n"
        val data: List<TestCsvRow> = listOf(
                TestCsvRow(1, "a","one"),
                TestCsvRow(2, "b", "two"),
                TestCsvRow(3, "c", "three")
        )

        val exporter = buildCsv<TestCsvRow> {
            configure {
                includeHeader = false
            }

            addColumn {
                name = "left"
                translate = { it.id.toString() }
            }

            addColumn {
                name = "middle"
                translate = { it.leftValue }
            }

            addColumn {
                name = "right"
                translate = { it.rightValue }
            }
        }

        // Execute
        val csv = exporter.toCsv(data)

        // Verify
        assert(csv == expectedCsv) {
            "The generated csv did not match.\n\texpected=${expectedCsv}\n\tactual=${csv}"
        }
    }

    @Test
    fun testMissingHeaderValue() {
        // Setup
        val data = listOf(TestCsvRow(1, "a","one"))

        val columnDefinitions: Array<CsvColumn<TestCsvRow>> = arrayOf(
                CsvColumn("left") { it.id.toString() },
                CsvColumn("") { it.leftValue }, // Missing!
                CsvColumn("right") { it.rightValue }
        )

        val exporter = getCsvTestResource(columnDefinitions = columnDefinitions)

        // Execute / Verify
        assertThrows<CsvMissingHeaderException>("\"Can't produce unit-test csv, one or more required column headers are missing.\"") {
            exporter.toCsv(data)
        }
    }

    fun <T> getCsvTestResource(
            debugName: String = "unit-test",
            columnDefinitions: Array<CsvColumn<T>>,
            configuration: CsvConfig? = null
    ): CsvResource<T> {
        return object : CsvResource<T>(debugName) {
            override fun columnTranslations(): Array<CsvColumn<T>> = columnDefinitions
            override fun configureCsv(): CsvConfig = configuration ?: super.configureCsv()
        }
    }
}