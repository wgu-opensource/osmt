package edu.wgu.osmt.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.time.ZoneOffset


interface BaseTable {
    val table: LongIdTable

    // id provided by LongIdTable
    val creationDate: Column<LocalDateTime>
}

interface TableWithInsertMapper<T : DatabaseData> : BaseTable {

    fun insertStatementApplyFromT(insertStatement: InsertStatement<Number>, t: T) {
        // deal with id and metadata
        t.id?.let { throw Exception("tried to insert an object with an existing id") }
        insertStatement[creationDate] = t.creationDate
    }

    fun insert(t: T): Long = transaction {
        val id = table.insert {
            insertStatementApplyFromT(it, t)
        } get table.id
        id.value
    }
}

interface TableWithUpdateMapper<UpdateObjectType : UpdateObject<*>> : BaseTable {
    val updateDate: Column<LocalDateTime>

    fun updateBuilderApplyFromUpdateObject(updateBuilder: UpdateBuilder<Number>, updateObject: UpdateObjectType) {
        updateBuilder[updateDate] = LocalDateTime.now(ZoneOffset.UTC)
    }

    fun update(updateObject: UpdateObjectType): Int = transaction {
        table.update({ table.id eq updateObject.id }) {
            updateBuilderApplyFromUpdateObject(it, updateObject)
        }
    }
}
