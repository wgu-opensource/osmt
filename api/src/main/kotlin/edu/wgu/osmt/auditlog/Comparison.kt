package edu.wgu.osmt.auditlog

import kotlin.reflect.*

const val DELIMITER = "; "

data class Comparison<R>(val fieldName: String, val function: KFunction1<R, String?>, val old: R?, val new: R?) {
    fun compare(): Change? {
        val oldValue: String? = old?.let { function(it) }
        val newValue: String? = new?.let { function(it) }
        return if (oldValue != newValue) {
            Change.maybeChange(fieldName, oldValue, newValue)
        } else null
    }
}
