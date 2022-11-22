package edu.wgu.osmt.util

import edu.wgu.osmt.config.CSV_FILE_EXTENSION
import edu.wgu.osmt.config.LIBRARY_EXPORT_PREFIX
import edu.wgu.osmt.config.RESOURCE_PATH
import org.joda.time.DateTime
import java.io.File

class OsmtUtils {

    companion object {
        fun generateCsvFileName() : String {
            val fileLocation: String = File(RESOURCE_PATH).absolutePath + "/"
            return fileLocation.plus(LIBRARY_EXPORT_PREFIX).plus(DateTime.now().toString()).plus(CSV_FILE_EXTENSION)
        }
    }
}