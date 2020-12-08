package edu.wgu.osmt.db

import edu.wgu.osmt.auditlog.Change
import edu.wgu.osmt.keyword.Keyword
import net.minidev.json.JSONArray
import org.jetbrains.exposed.dao.LongEntity
import kotlin.reflect.KProperty0

interface UpdateObject<T> {
    val id: Long?
}

interface HasPublishStatus {
    val publishStatus: PublishStatus?
}

interface Compares<T> where T : LongEntity {
    val delimiter: String
        get() = "; "

    val comparisonList: List<(t: T) -> Change?>

    val stringOutput: (String?) -> String?
        get() = { s: String? -> s }

    val keywordOutput: (Keyword?) -> String?
        get() = { k: Keyword? -> k?.value }

    fun <R : String?> change(
        old: KProperty0<R>,
        new: KProperty0<R>
    ): Change? {
        return change(old, new, stringOutput)
    }

    fun <R : Any?> change(
        old: KProperty0<R>,
        new: KProperty0<R>,
        outputR: (R) -> String?
    ): Change? {
        return if (outputR(new.get()) != outputR(old.get())) Change(
            old.name,
            outputR(old.get()),
            outputR(new.get())
        ) else null
    }

    fun change(name: String, old: String?, new: String?): Change? {
        return if (old != new) {
            Change(name, old, new)
        } else null
    }

    fun diff(that: T): List<Change> {
        return comparisonList.mapNotNull { it(that) }
    }
}
