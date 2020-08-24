package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.BaseTable
import edu.wgu.osmt.db.PublishStatusTable
import edu.wgu.osmt.db.TableWithUpdateMapper
import edu.wgu.osmt.jobcode.JobCodeTable
import edu.wgu.osmt.keyword.KeywordTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder


object RichSkillDescriptorTable : TableWithUpdateMapper<RsdUpdateObject>, LongIdTable("RichSkillDescriptor") {
    override val table = this

    override val creationDate = datetime("creationDate")
    override val updateDate = datetime("updateDate")
    val uuid = varchar("uuid", 36, BaseTable.defaultCollation).uniqueIndex()
    val name = text("name", BaseTable.defaultCollation)
    val statement = text("statement", BaseTable.defaultCollation)
    val category = reference("cat_id", KeywordTable, ReferenceOption.RESTRICT).nullable()
    val author = text("author", BaseTable.defaultCollation)
    val publishStatus = reference("publish_status_id", PublishStatusTable, ReferenceOption.RESTRICT)

    override fun updateBuilderApplyFromUpdateObject(
        updateBuilder: UpdateBuilder<Number>,
        updateObject: RsdUpdateObject
    ) {
        super.updateBuilderApplyFromUpdateObject(updateBuilder, updateObject)
        updateObject.name?.let { updateBuilder[name] = it }
        updateObject.statement?.let { updateBuilder[statement] = it }
        updateObject.category?.let {
            if (it.t != null) {
                updateBuilder[category] = EntityID<Long>(it.t.id!!, KeywordTable)
            } else {
                updateBuilder[category] = null
            }
        }
        updateObject.author?.let { updateBuilder[author] = it }
    }
}

// many-to-many table for RichSkillDescriptor and JobCode relationship
object RichSkillJobCodes : Table("RichSkillJobSkills") {
    val id: Column<Long> = long("id").uniqueIndex()
    val richSkillId = reference("richskill_id", RichSkillDescriptorTable, onDelete = ReferenceOption.CASCADE)
    val jobCodeId = reference("jobcode_id", JobCodeTable, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(richSkillId, jobCodeId, name = "PK_RichSkillJobCodes_rs_jc")
}

object RichSkillKeywords : Table("RichSkillKeywords") {
    val id: Column<Long> = long("id").uniqueIndex()
    val richSkillId = reference("richskill_id", RichSkillDescriptorTable, onDelete = ReferenceOption.CASCADE)
    val keywordId = reference("keyword_id", KeywordTable, onDelete = ReferenceOption.CASCADE)
}

