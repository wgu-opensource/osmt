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

    override fun toRow(updateBuilder: UpdateBuilder<Number>, t: RichSkillDescriptor){
        t.id?.let{updateBuilder[id] = it}
        updateBuilder[title] = t.title
        updateBuilder[description] = t.description
    }
}
