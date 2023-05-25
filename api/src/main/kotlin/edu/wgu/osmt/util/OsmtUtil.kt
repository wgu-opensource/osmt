package edu.wgu.osmt.util

import edu.wgu.osmt.config.CLOSING_BRACKET
import edu.wgu.osmt.config.COMMA
import edu.wgu.osmt.config.OPENING_BRACKET
import edu.wgu.osmt.config.SEMICOLON
import org.apache.commons.lang3.StringUtils

class OsmtUtil {

    companion object {
        fun parseMultiValueToSingleValue(field: String) : String {
            return StringUtils.replace(
                    StringUtils.replace(
                            StringUtils.replace(field, OPENING_BRACKET, StringUtils.EMPTY),
                            COMMA.plus(CLOSING_BRACKET), StringUtils.EMPTY),
                    COMMA.plus(COMMA).plus(StringUtils.SPACE), SEMICOLON)
        }
    }
}