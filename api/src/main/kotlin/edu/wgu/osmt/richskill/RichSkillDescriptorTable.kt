package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.PublishStatusTable
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
    val category = reference(
        "cat_id",
        KeywordTable,
        onDelete = ReferenceOption.RESTRICT,
        onUpdate = ReferenceOption.CASCADE
    ).nullable()
    val author = reference(
        "author_id",
        KeywordTable,
        onDelete = ReferenceOption.RESTRICT,
        onUpdate = ReferenceOption.CASCADE
    ).nullable()
    val publishStatus = reference(
        "publish_status_id",
        PublishStatusTable,
        onDelete = ReferenceOption.RESTRICT,
        onUpdate = ReferenceOption.CASCADE
    )

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
        updateObject.author?.let {
            if (it.t != null) {
                updateBuilder[author] = EntityID<Long>(it.t.id!!, KeywordTable)
            } else {
                updateBuilder[author] = null
            }
        }
    }
}

// many-to-many table for RichSkillDescriptor and JobCode relationship
object RichSkillJobCodes : Table("RichSkillJobCodes") {
    val richSkillId = reference(
        "richskill_id",
        RichSkillDescriptorTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).index()
    val jobCodeId = reference(
        "jobcode_id",
        JobCodeTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).index()
    override val primaryKey = PrimaryKey(richSkillId, jobCodeId, name = "PK_RichSkillJobCodes_rs_jc")

    fun create(richSkillId: Long, jobCodeId: Long) {
        insert {
            it[this.richSkillId] = EntityID(richSkillId, RichSkillDescriptorTable)
            it[this.jobCodeId] = EntityID(jobCodeId, JobCodeTable)
        }
    }

    fun delete(richSkillId: Long, jobCodeId: Long) {
        deleteWhere {
            (RichSkillJobCodes.richSkillId eq richSkillId) and (RichSkillJobCodes.jobCodeId eq jobCodeId)
        }
    }
}

object RichSkillKeywords : Table("RichSkillKeywords") {
    val richSkillId = reference(
        "richskill_id",
        RichSkillDescriptorTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).index()
    val keywordId = reference(
        "keyword_id",
        KeywordTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).index()
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

