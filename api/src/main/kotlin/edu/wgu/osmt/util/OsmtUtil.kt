package edu.wgu.osmt.util

import org.apache.commons.lang3.StringUtils

class OsmtUtil {

    companion object {
        fun parseMultiValueToSingleValue(field: String) : String {
            return StringUtils.replace(
                    StringUtils.replace(
                            StringUtils.replace(
                                    field, "[", StringUtils.EMPTY
                            ), ",]", StringUtils.EMPTY
                    ), ",, ", ";"
            )
        }
    }
}