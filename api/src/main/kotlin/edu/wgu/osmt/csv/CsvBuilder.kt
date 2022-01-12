package edu.wgu.osmt.csv

/**
 * DSL style entry-point to build a CsvResource.  This approach should be used if a csv file is simple in structure
 * and can be defined inline where it's used, rather than as a top level type definition.
 *
 * Unlike implementing CsvResource with a concrete type, this function produces an anonymous class implementation
 * so it probably shouldn't be invoked repeatedly for identical document shapes, like in a loop.
 */
fun <T> buildCsv(lambda: CsvResourceBuilder<T>.() -> Unit): CsvResource<T> = CsvResourceBuilder<T>().apply(lambda).build()

class CsvResourceBuilder<T> {
    var name: String = ""
    private val data: MutableList<CsvColumn<T>> = mutableListOf()
    private var config: CsvConfig = CsvConfig()

    /**
     * Add a new column to the right side of the csv.
     */
    fun addColumn(lamdba: CsvColumnBuilder<T>.() -> Unit): CsvResourceBuilder<T> {
        data.add(CsvColumnBuilder<T>().apply(lamdba).build())
        return this
    }

    /**
     * Configure the global properties of this csv export.
     */
    fun configure(lambda: CsvConfigBuilder<T>.() -> Unit): CsvResourceBuilder<T> {
        config = CsvConfigBuilder<T>().apply(lambda).build()
        return this
    }

    fun build(): CsvResource<T> {
        return object : CsvResource<T>(name) {
            override fun columnTranslations(d: List<T>): Array<CsvColumn<T>> = data.toTypedArray()
            override fun configureCsv(): CsvConfig = config
        }
    }
}

class CsvColumnBuilder<T> {
    var name = ""
    var translate: (T) -> String = { t -> t.toString() }

    fun build(): CsvColumn<T> {
        return CsvColumn(name, translate)
    }
}

class CsvConfigBuilder<T> {
    var delimeter: Char = CsvConfig.delimeter
    var quoteChar: Char = CsvConfig.quoteChar
    var escapeChar: Char = CsvConfig.escapeChar
    var lineEnd: String = CsvConfig.lineEnd
    var includeHeader: Boolean = CsvConfig.includeHeader

    fun build(): CsvConfig {
        return CsvConfig(delimeter, quoteChar, escapeChar, lineEnd, includeHeader)
    }
}