package edu.wgu.osmt.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.time.ZoneOffset


interface BaseTable {
    val creationDate: Column<LocalDateTime>
}


interface TableWithInsert<T : DatabaseData> : BaseTable {
    fun insertStatementApplyFromT(insertStatement: InsertStatement<Number>, t: T) {
        // deal with id and metadata
        t.id?.let { throw Exception("tried to insert an object with an existing id") }
        insertStatement[creationDate] = t.creationDate
    }
}

interface TableWithUpdate<UpdateObjectType : UpdateObject<*>> : BaseTable {
    val updateDate: Column<LocalDateTime>

    fun updateBuilderApplyFromUpdateObject(updateBuilder: UpdateBuilder<Number>, updateObject: UpdateObjectType) {
        updateBuilder[updateDate] = LocalDateTime.now(ZoneOffset.UTC)
    }
}

fun <T, R> T.updateFromObject(updateObject: R): Int where T: LongIdTable, T: TableWithUpdate<R>, R: UpdateObject<*> {
    return update({ id eq updateObject.id}){
        updateBuilderApplyFromUpdateObject(it, updateObject)
    }
}

fun <T, R> T.insertObject(r: R): Long where T: LongIdTable, T: TableWithInsert<R>, R: DatabaseData{
    val id = insert { insertStatementApplyFromT(it, r) } get id
    return id.value
}

