package edu.wgu.osmt.jobcode

import edu.wgu.osmt.db.BaseTable
import edu.wgu.osmt.db.TableWithUpdateMapper
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder

object JobCodeTable : TableWithUpdateMapper<JobCodeUpdate>, LongIdTable("JobCode") {
    override val table = this

    override val creationDate = datetime("creationDate")
    override val updateDate = datetime("updateDate")
    val code: Column<String> = varchar("code", 128)
    val name: Column<String?> = varchar("name", 128).nullable()
    val description: Column<String?> = text("description").nullable()
    val sourceColumn: Column<String?> = varchar("source", 1024).nullable()


    override fun updateBuilderApplyFromUpdateObject(updateBuilder: UpdateBuilder<Number>, updateObject: JobCodeUpdate) {
        super.updateBuilderApplyFromUpdateObject(updateBuilder, updateObject)
        updateObject.code?.let { updateBuilder[code] = it }
        updateObject.name?.let { it.t?.let { updateBuilder[name] = it } }
        updateObject.description?.let { it.t?.let { updateBuilder[description] = it } }
        updateObject.source?.let { it.t?.let { updateBuilder[sourceColumn] = it } }
    }
}
