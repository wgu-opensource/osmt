package edu.wgu.osmt.jobcode

import edu.wgu.osmt.db.TableWithUpdateMapper
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder

object JobCodeTable : TableWithUpdateMapper<JobCodeUpdate>, LongIdTable("JobCode") {
    override val table = this

    override val creationDate = datetime("creationDate")
    override val updateDate = datetime("updateDate")

    val major: Column<String?> = varchar("major", 1024).nullable()
    val minor: Column<String?> = varchar("minor", 1024).nullable()
    val broad: Column<String?> = varchar("broad", 1024).nullable()
    val detailed: Column<String?> = varchar("detailed", 1024).nullable()

    val code: Column<String> = varchar("code", 128).index()
    val name: Column<String?> = varchar("name", 1024).nullable()
    val description: Column<String?> = text("description").nullable()
    val framework: Column<String?> = varchar("framework", 1024).nullable()
    val url: Column<String?> = varchar("url", 1024).nullable()


    override fun updateBuilderApplyFromUpdateObject(updateBuilder: UpdateBuilder<Number>, updateObject: JobCodeUpdate) {
        super.updateBuilderApplyFromUpdateObject(updateBuilder, updateObject)
        updateObject.major?.let { it.t?.let { updateBuilder[major] = it } }
        updateObject.minor?.let { it.t?.let { updateBuilder[minor] = it } }
        updateObject.broad?.let { it.t?.let { updateBuilder[broad] = it } }
        updateObject.detailed?.let { it.t?.let { updateBuilder[detailed] = it } }

        updateObject.code?.let { updateBuilder[code] = it }
        updateObject.name?.let { it.t?.let { updateBuilder[name] = it } }
        updateObject.description?.let { it.t?.let { updateBuilder[description] = it } }
        updateObject.framework?.let { it.t?.let { updateBuilder[framework] = it } }
        updateObject.url?.let { fieldUpdate -> fieldUpdate.t?.let { updateBuilder[url] = it } }
    }
}
