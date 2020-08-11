package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.TableWithUpdateMapper
import edu.wgu.osmt.jobcode.JobCodeTable
import edu.wgu.osmt.keyword.KeywordTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder


object RichSkillDescriptorTable : TableWithUpdateMapper<RichSkillDescriptor, RsdUpdateObject>("RichSkillDescriptor") {
    val title = text("title")
    val description = text("description")
    val category = reference("cat_id", KeywordTable, ReferenceOption.RESTRICT).nullable()

    override fun updateBuilderApplyFromUpdateObject(
        updateBuilder: UpdateBuilder<Number>,
        updateObject: RsdUpdateObject
    ) {
        super.updateBuilderApplyFromUpdateObject(updateBuilder, updateObject)
        updateObject.title?.let { updateBuilder[title] = it }
    }

    override fun insertStatementApplyFromT(insertStatement: InsertStatement<Number>, t: RichSkillDescriptor) {
        super.insertStatementApplyFromT(insertStatement, t)
        insertStatement[title] = t.title
        insertStatement[description] = t.description
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

