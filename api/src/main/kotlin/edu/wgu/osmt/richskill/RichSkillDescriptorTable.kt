package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.TableWithMappers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset


@Service
class RichSkillDescriptorTable: TableWithMappers<RichSkillDescriptor, RsdUpdateObject>("RichSkillDescriptor") {

    val title = text("title")
    val description = text("description")
    val nullableField = text("nullableField").nullable()
    val updateDate = datetime("updateDate")

    override fun fromRow(t: ResultRow): RichSkillDescriptor = RichSkillDescriptor(
            title = t[title],
            description = t[description],
            id = t[id],
            creationDate = t[creationDate],
            updateDate = t[updateDate],
            nullableField = t[nullableField]
    )

    override fun toRowFromUpdateObject(updateBuilder: UpdateBuilder<Number>, updateObject: RsdUpdateObject){
        updateObject.title?.let{updateBuilder[title] = it}
        updateBuilder[updateDate] = LocalDateTime.now(ZoneOffset.UTC)
    }

    override fun toRowFromT(updateBuilder: UpdateBuilder<Number>, t: RichSkillDescriptor){
        super<TableWithMappers>.toRowFromT(updateBuilder, t)
        updateBuilder[updateDate] = LocalDateTime.now(ZoneOffset.UTC)
        updateBuilder[title] = t.title
        updateBuilder[description] = t.description
    }
}
