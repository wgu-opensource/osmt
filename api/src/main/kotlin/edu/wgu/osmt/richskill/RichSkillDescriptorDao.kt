package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.OutputsModel
import edu.wgu.osmt.jobcode.JobCodeDao
import edu.wgu.osmt.keyword.KeywordDao
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime

class RichSkillDescriptorDao(id: EntityID<Long>) : LongEntity(id), OutputsModel<RichSkillDescriptor> {
    companion object : LongEntityClass<RichSkillDescriptorDao>(RichSkillDescriptorTable)

    var creationDate: LocalDateTime by RichSkillDescriptorTable.creationDate
    var updateDate: LocalDateTime by RichSkillDescriptorTable.updateDate
    var title: String by RichSkillDescriptorTable.title
    var description: String by RichSkillDescriptorTable.description

    var jobCodes by JobCodeDao via RichSkillJobCodes

    var keywords by KeywordDao via RichSkillKeywords

    override fun toModel(): RichSkillDescriptor {
        return RichSkillDescriptor(
            id = id.value,
            creationDate = creationDate,
            updateDate = updateDate,
            title = title,
            description = description,
            jobCodes = jobCodes.map { it.toModel() },
            keywords = keywords.map { it.toModel() }
        )
    }
}
