package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.BaseTable
import edu.wgu.osmt.db.PublishStatusTable
import edu.wgu.osmt.db.TableWithInsertMapper
import edu.wgu.osmt.db.TableWithUpdateMapper
import edu.wgu.osmt.jobcode.JobCodeTable
import edu.wgu.osmt.keyword.KeywordTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder


object RichSkillDescriptorTable : TableWithUpdateMapper<RsdUpdateObject>, LongIdTable("RichSkillDescriptor") {
    override val table = this

    override val creationDate = datetime("creationDate")
    override val updateDate = datetime("updateDate")
    val uuid = varchar("uuid", 36).uniqueIndex()
    val name = text("name")
    val statement = text("statement")
    val category = reference("cat_id", KeywordTable, ReferenceOption.RESTRICT).nullable()
    val author = text("author")
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
    val richSkillId = reference("richskill_id", RichSkillDescriptorTable, onDelete = ReferenceOption.CASCADE).index()
    val jobCodeId = reference("jobcode_id", JobCodeTable, onDelete = ReferenceOption.CASCADE).index()
    override val primaryKey = PrimaryKey(richSkillId, jobCodeId, name = "PK_RichSkillJobCodes_rs_jc")
}

object RichSkillKeywords : LongIdTable("RichSkillKeywords") {
    val richSkillId = reference("richskill_id", RichSkillDescriptorTable, onDelete = ReferenceOption.CASCADE).index()
    val keywordId = reference("keyword_id", KeywordTable, onDelete = ReferenceOption.CASCADE).index()
    override val primaryKey = PrimaryKey(richSkillId, keywordId, name = "PK_RichSkillKeywords_rs_kw")

    fun create(richSkillId: Long, keywordId: Long) {
        insert {
            it[this.richSkillId] = EntityID(richSkillId, RichSkillDescriptorTable)
            it[this.keywordId] = EntityID(keywordId, KeywordTable)
        }
    }

    fun delete(richSkillId: Long, keywordId: Long) {
        deleteWhere {
            (RichSkillKeywords.richSkillId eq richSkillId) and
            (RichSkillKeywords.keywordId eq keywordId)
        }
    }
}

