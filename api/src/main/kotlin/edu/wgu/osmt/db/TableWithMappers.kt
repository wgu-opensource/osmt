package edu.wgu.osmt.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.time.LocalDateTime
import java.time.ZoneOffset


abstract class TableWithMappers<T : DatabaseData<T>>(name: String) : LongIdTable(name) {
    // id provided by LongIdTable
    val creationDate = datetime("creationDate")

    open fun insertStatementApplyFromT(insertStatement: InsertStatement<Number>, t: T) {
        // deal with id and metadata
        t.id?.let { throw Exception("tried to insert an object with an existing id") }
        insertStatement[creationDate] = t.creationDate
    }
}

abstract class TableWithUpdateMapper<T : DatabaseData<T>, in UpdateObjectType : UpdateObject>(name: String) :
    TableWithMappers<T>(name) {

    val updateDate = datetime("updateDate")

    override fun insertStatementApplyFromT(insertStatement: InsertStatement<Number>, t: T) {
        super.insertStatementApplyFromT(insertStatement, t)
        if (t is HasUpdateDate) {
            insertStatement[updateDate] = t.updateDate
        }
    }

    open fun updateBuilderApplyFromUpdateObject(updateBuilder: UpdateBuilder<Number>, updateObject: UpdateObjectType) {
        updateBuilder[updateDate] = LocalDateTime.now(ZoneOffset.UTC)
    }
}
