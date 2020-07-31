package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.TableWithUpdateMapper
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Service


@Service
class RichSkillDescriptorTable : TableWithUpdateMapper<RichSkillDescriptor, RsdUpdateObject>("RichSkillDescriptor") {
    val title = text("title")
    val description = text("description")
    val nullableField = text("nullableField").nullable()

    override fun fromRow(t: ResultRow): RichSkillDescriptor = RichSkillDescriptor(
        id = t[id],
        creationDate = t[creationDate],
        updateDate = t[updateDate],
        title = t[title],
        description = t[description],
        nullableField = t[nullableField]
    )

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
