package edu.wgu.osmt.jobcode

// While most codes follow one of these exact formats xx-xxxx or xx-xxxx.xx. I've found some that have a single
// major code like x-xxxx.xx which means that a little extra processing needs to be done since the character's indexes
// in the string are not 100% consistent
object JobCodeBreakout {

    private fun majorPart(code: String): String? = code.split("-")
            .takeIf { it.isNotEmpty() }
            ?.let { it[0] }
            ?.takeIf { it.toIntOrNull() != null }

    private fun minorPart(code: String): String? = code.split("-")
            .takeIf { it.size > 1 }
            ?.let { it[1] }
            ?.takeIf { it.length >= 2 }
            ?.substring(0, 2)
            ?.takeIf { it.toIntOrNull() != null }

    private fun broadPart(code: String): String? = code.split("-")
            .takeIf { it.size > 1 }
            ?.let { it[1] }
            ?.takeIf { it.length >= 3 }
            ?.substring(2, 3)
            ?.takeIf { it.toIntOrNull() != null }


    private fun detailedPart(code: String): String? = code.split("-")
            .takeIf { it.size > 1 }
            ?.let { it[1] }
            ?.takeIf { it.length >= 4 }
            ?.substring(3, 4)
            ?.takeIf { it.toIntOrNull() != null }

    private fun jobRolePart(code: String): String? = ".*\\.(\\d{2})$".toRegex() // captures group: xx-xxxx.(xx)
            .find(code)
            ?.groupValues
            ?.takeIf { it.size > 1 }
            ?.let { it[1] }
            ?.takeIf { it.toIntOrNull() != null }

    // For use on major, minor, broad and detailed codes to determine if it's
    private fun isValidInt(value: String?): Boolean = value
            ?.toIntOrNull()
            ?.let { intOrNull -> intOrNull > 0 }
            ?: false

    // One edge case to note here for these 5 functions below:
    //
    // For each part in the hierarchy these functions trust that parent code parts are present and valid when generating
    // a child code.  For example: A code of 00-1110 would produce a broad and minor code of 00-1100 and 00-1110, but
    // currently should not produce a major code 00-0000.
    fun majorCode(code: String): String? = majorPart(code)
            ?.takeIf { isValidInt(it) }
            ?.let { "${it}-0000" }

    fun minorCode(code: String): String? = minorPart(code)
            ?.takeIf { isValidInt(it)}
            ?.let { "${majorPart(code)}-${minorPart(code)}00" }

    fun broadCode(code: String): String? = broadPart(code)
            ?.takeIf { isValidInt(it) }
            ?.let { "${majorPart(code)}-${minorPart(code)}${it}0" }

    fun detailedCode(code: String): String? = detailedPart(code)
            ?.takeIf { isValidInt(it) }
            ?.let { "${majorPart(code)}-${minorPart(code)}${broadPart(code)}${it}" }

    fun jobRoleCode(code: String): String? = jobRolePart(code)
            ?.let { "${this.majorPart(code)}-${this.minorPart(code)}${this.broadPart(code)}${this.detailedPart(code)}.${it}" }
}