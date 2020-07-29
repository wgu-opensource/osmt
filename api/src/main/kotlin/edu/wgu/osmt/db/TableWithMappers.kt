package edu.wgu.osmt.db

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder


abstract class TableWithMappers<T : DatabaseData<T>, in UpdateObjectType : UpdateObject>(name: String) : Table(name) {
    val id = long("id").autoIncrement().primaryKey()
    val creationDate = datetime("creationDate")

    abstract fun fromRow(t: ResultRow): T

    abstract fun toRowFromUpdateObject(updateBuilder: UpdateBuilder<Number>, updateObject: UpdateObjectType)

    open fun toRowFromT(updateBuilder: UpdateBuilder<Number>, t: T) {
        // deal with id and metadata
        t.id?.let { updateBuilder[id] = it }
        updateBuilder[creationDate] = t.creationDate
    }
}
