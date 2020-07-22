package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.TableWithMappers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Service


@Service
class RichSkillDescriptorTable: TableWithMappers<RichSkillDescriptor>("RichSkillDescriptor") {

    val title = text("title")
    val description = text("description")

    override fun fromRow(r: ResultRow): RichSkillDescriptor = RichSkillDescriptor(
            r[title],
            r[description],
            r[id]
    )

    override fun toRow(it: UpdateBuilder<Number>, t: RichSkillDescriptor){
        if (t.id != null) it[id] = t.id
        it[title] = t.title
        it[description] = t.description
    }
}
