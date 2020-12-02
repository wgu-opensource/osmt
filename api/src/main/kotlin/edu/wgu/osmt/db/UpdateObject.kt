package edu.wgu.osmt.db

import edu.wgu.osmt.keyword.Keyword
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import kotlin.reflect.KProperty0

interface UpdateObject<T> {
    val id: Long?

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
        return if (thisProp.get() != thatProp.get()) jsonUpdateStatement(
            thatProp.name,
            outputR(thatProp.get()),
            outputR(thisProp.get())
        ) else null
    }

    fun jsonUpdateStatement(fieldName: String, oldValue: String?, newValue: String?): JSONObject {
        return JSONObject(mutableMapOf(fieldName to mutableMapOf("old" to oldValue, "new" to newValue)))
    }

    fun diff(that: T): JSONArray {
        val jsonObj = JSONArray()
        comparisonList.forEach { it(that)?.let { jsonObj.add(it) } }
        return jsonObj
    }
}

interface HasPublishStatus{
    val publishStatus: PublishStatus?
}


