package edu.wgu.osmt.util

import org.apache.commons.lang3.StringUtils

class OsmtUtil {


    companion object {
        private const val OPENING_BRACKET = "["
        private const val CLOSING_BRACKET = "]"
        private const val COMMA = ","
        private const val SEMICOLON = ";"

        fun parseMultiValueToSingleValue(field: String) : String {
            return StringUtils.replace(
                    StringUtils.replace(
                            StringUtils.replace(field, OPENING_BRACKET, StringUtils.EMPTY),
                            COMMA.plus(CLOSING_BRACKET), StringUtils.EMPTY
                    ),
                    COMMA.plus(COMMA).plus(StringUtils.SPACE), SEMICOLON
            )
        }


    }
}