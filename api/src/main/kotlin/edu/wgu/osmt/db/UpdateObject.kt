package edu.wgu.osmt.db

import edu.wgu.osmt.keyword.Keyword
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import kotlin.reflect.KProperty0

interface UpdateObject<T : DatabaseData<T>> {
    val id: Long

    val comparisonList: List<(t: T) -> JSONObject?>

    val stringOutput: (String?) -> String?
        get() = { s: String? -> s }

    val keywordOutput: (Keyword?) -> String?
        get() = { k: Keyword? -> k?.value }

    fun <R : String?> compare(
        thatProp: KProperty0<R>,
        thisProp: KProperty0<R>
    ): JSONObject? {
        return compare(thatProp, thisProp, stringOutput)
    }

    fun <R : Any?> compare(
        thatProp: KProperty0<R>,
        thisProp: KProperty0<R>,
        outputR: (R) -> String?
    ): JSONObject? {
        return if (thisProp.get() != thatProp.get()) JSONObject(
            mutableMapOf(
                thatProp.name to mutableMapOf(
                    "old" to outputR(thatProp.get()),
                    "new" to outputR(thisProp.get())
                )
            )
        ) else null
    }

    fun diff(that: T): JSONArray {
        val jsonObj = JSONArray()
        comparisonList.forEach { it(that)?.let { jsonObj.add(it) } }
        return jsonObj
    }
}


