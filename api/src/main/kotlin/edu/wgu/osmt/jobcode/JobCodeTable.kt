package edu.wgu.osmt.jobcode

import edu.wgu.osmt.db.TableWithUpdateMapper
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder

object JobCodeTable : TableWithUpdateMapper<JobCode, JobCodeUpdate>("JobCode") {
    val code: Column<String> = text("code")
    val name: Column<String?> = text("name").nullable()
    val description: Column<String?> = text("description").nullable()
    val sourceColumn: Column<String?> = text("source").nullable()


    override fun updateBuilderApplyFromUpdateObject(updateBuilder: UpdateBuilder<Number>, updateObject: JobCodeUpdate) {
        super.updateBuilderApplyFromUpdateObject(updateBuilder, updateObject)
        updateObject.code?.let { updateBuilder[code] = it }
        updateObject.name?.let { it.t?.let { updateBuilder[name] = it } }
        updateObject.description?.let { it.t?.let { updateBuilder[description] = it } }
        updateObject.source?.let { it.t?.let { updateBuilder[sourceColumn] = it } }
    }

    override fun insertStatementApplyFromT(insertStatement: InsertStatement<Number>, t: JobCode) {
        super.insertStatementApplyFromT(insertStatement, t)
        insertStatement[code] = t.code
        insertStatement[name] = t.name
        insertStatement[description] = t.description
        insertStatement[sourceColumn] = t.source
    }
}
