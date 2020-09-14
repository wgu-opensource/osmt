package edu.wgu.osmt.richskill

import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.collection.CollectionSkills
import edu.wgu.osmt.db.OutputsModel
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.db.PublishStatusDao
import edu.wgu.osmt.jobcode.JobCodeDao
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordTypeEnum
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime
import java.util.*

class RichSkillDescriptorDao(id: EntityID<Long>) : LongEntity(id), OutputsModel<RichSkillDescriptor> {
    companion object : LongEntityClass<RichSkillDescriptorDao>(RichSkillDescriptorTable)

    var creationDate: LocalDateTime by RichSkillDescriptorTable.creationDate
    var updateDate: LocalDateTime by RichSkillDescriptorTable.updateDate

    var uuid: String by RichSkillDescriptorTable.uuid
    var name: String by RichSkillDescriptorTable.name
    var statement: String by RichSkillDescriptorTable.statement
    var author by KeywordDao optionalReferencedOn RichSkillDescriptorTable.author

    var jobCodes by JobCodeDao via RichSkillJobCodes

    var keywords by KeywordDao via RichSkillKeywords

    var category by KeywordDao optionalReferencedOn RichSkillDescriptorTable.category

    var publishStatus by PublishStatusDao referencedOn RichSkillDescriptorTable.publishStatus

    var collections by CollectionDao via CollectionSkills

    override fun toModel(): RichSkillDescriptor {
        return RichSkillDescriptor(
            id = id.value,
            creationDate = creationDate,
            updateDate = updateDate,
            uuid = UUID.fromString(uuid),
            name = name,
            statement = statement,
            jobCodes = jobCodes.map { it.toModel() },
            keywords = keywords.map { it.toModel() },
            category = category?.toModel(),
            author = author?.toModel(),
            publishStatus = PublishStatus.valueOf(publishStatus.name)
        )
    }
}
