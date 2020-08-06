package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.TableWithUpdateMapper
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.beans.factory.annotation.Autowired


object RichSkillDescriptorTable : TableWithUpdateMapper<RichSkillDescriptor, RsdUpdateObject>("RichSkillDescriptor") {
    val title = text("title")
    val description = text("description")

    override fun fromRow(t: ResultRow): RichSkillDescriptor {
        return RichSkillDescriptor(
            id = t[id].value,
            creationDate = t[creationDate],
            updateDate = t[updateDate],
            title = t[title],
            description = t[description]
        )
    }

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
    val richSkillId = reference("richskill_id", RichSkillDescriptorTable)
    val jobCodeId = reference("jobcode_id", JobCodeTable)
    override val primaryKey = PrimaryKey(richSkillId, jobCodeId, name = "PK_RichSkillJobCodes_rs_jc")
}
