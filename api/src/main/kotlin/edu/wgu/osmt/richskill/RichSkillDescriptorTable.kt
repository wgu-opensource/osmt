package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.PublishStatusUpdate
import edu.wgu.osmt.db.TableWithUpdate
import edu.wgu.osmt.jobcode.JobCodeTable
import edu.wgu.osmt.keyword.KeywordTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime


object RichSkillDescriptorTable : LongIdTable("RichSkillDescriptor"), TableWithUpdate<RsdUpdateObject>,
    PublishStatusUpdate<RsdUpdateObject> {

    override val creationDate = datetime("creationDate")
    override val updateDate = datetime("updateDate")

    override val archiveDate: Column<LocalDateTime?> = datetime("archiveDate").nullable()
    override val publishDate: Column<LocalDateTime?> = datetime("publishDate").nullable()

    val uuid = varchar("uuid", 36).uniqueIndex()
    val name = text("name")
    val statement = text("statement")
}

object RichSkillAuthors : Table("RichSkillAuthors") {
    val richSkillId = reference(
        "richskill_id",
        RichSkillDescriptorTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).index()
    val authorId = reference(
        "author_id",

        KeywordTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).index()
    override val primaryKey = PrimaryKey(richSkillId, authorId, name = "PK_RichSkillAuthors_rs_au")

    fun create(richSkillId: Long, authorId: Long) {
        insertIgnore {
            it[this.richSkillId] = EntityID(richSkillId, RichSkillDescriptorTable)
            it[this.authorId] = EntityID(authorId, KeywordTable)
        }
    }

    fun delete(richSkillId: Long, authorId: Long) {
        deleteWhere {
            (RichSkillAuthors.richSkillId eq richSkillId) and
                    (RichSkillAuthors.authorId eq authorId)
        }
    }
}

object RichSkillCategories : Table("RichSkillCategories") {
    val richSkillId = reference(
        "richskill_id",
        RichSkillDescriptorTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).index()
    val categoryId = reference(
        "category_id",

        KeywordTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).index()
    override val primaryKey = PrimaryKey(richSkillId, categoryId, name = "PK_RichSkillCategory_rs_cg")

    fun create(richSkillId: Long, categoryId: Long) {
        insertIgnore {
            it[this.richSkillId] = EntityID(richSkillId, RichSkillDescriptorTable)
            it[this.categoryId] = EntityID(categoryId, KeywordTable)
        }
    }

    fun delete(richSkillId: Long, categoryId: Long) {
        deleteWhere {
            (RichSkillCategories.richSkillId eq richSkillId) and
                    (RichSkillCategories.categoryId eq categoryId)
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
        insertIgnore {
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
        insertIgnore {
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

