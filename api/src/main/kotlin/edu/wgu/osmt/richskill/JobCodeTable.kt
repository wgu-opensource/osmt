package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.TableWithUpdateMapper
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder

object JobCodeTable : TableWithUpdateMapper<JobCode, JobCodeUpdate>("JobCode") {
    val code: Column<String> = text("code")
    val name: Column<String?> = text("name").nullable()
    val description: Column<String?> = text("description").nullable()
    val sourceColumn: Column<String?> = text("source").nullable()

    override fun fromRow(t: ResultRow): JobCode = JobCode(
        id = t[id].value,
        creationDate = t[creationDate],
        updateDate = t[updateDate],
        code = t[code],
        name = t[name],
        description = t[description],
        source = t[sourceColumn]
    )

    // TODO
    fun maybeJobCodeInflator(rr: ResultRow): JobCode? {
        val id = rr.getOrNull(JobCodeTable.id)?.value
        val code = rr.getOrNull(code)
        val name = rr.getOrNull(name)
        val description = rr.getOrNull(description)
        val source = rr.getOrNull(sourceColumn)
        val creationDate = rr.getOrNull(JobCodeTable.creationDate)
        val updateDate = rr.getOrNull(JobCodeTable.updateDate)

        return if (id != null && code != null) {
            JobCode(
                id = id,
                code = code,
                creationDate = creationDate!!,
                updateDate = updateDate!!,
                name = name,
                description = description,
                source = source
            )
        } else null
    }

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
