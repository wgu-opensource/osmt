package edu.wgu.osmt.richskill

import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.collection.CollectionSkills
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.MutablePublishStatusDetails
import edu.wgu.osmt.db.OutputsModel
import edu.wgu.osmt.jobcode.JobCodeDao
import edu.wgu.osmt.keyword.KeywordDao
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime

class RichSkillDescriptorDao(id: EntityID<Long>) : LongEntity(id), OutputsModel<RichSkillDescriptor>,
    MutablePublishStatusDetails {
    companion object : LongEntityClass<RichSkillDescriptorDao>(RichSkillDescriptorTable)

    var creationDate: LocalDateTime by RichSkillDescriptorTable.creationDate
    var updateDate: LocalDateTime by RichSkillDescriptorTable.updateDate

    override var publishDate: LocalDateTime? by RichSkillDescriptorTable.publishDate
    override var archiveDate: LocalDateTime? by RichSkillDescriptorTable.archiveDate

    var uuid: String by RichSkillDescriptorTable.uuid
    var name: String by RichSkillDescriptorTable.name
    var statement: String by RichSkillDescriptorTable.statement
    var author by KeywordDao optionalReferencedOn RichSkillDescriptorTable.author
    var isExternallyShared: Boolean by RichSkillDescriptorTable.externallyShared
    var importedFrom: String? by RichSkillDescriptorTable.importedFrom
    var libraryName: String? by RichSkillDescriptorTable.libraryName

    var jobCodes by JobCodeDao via RichSkillJobCodes

    var keywords by KeywordDao via RichSkillKeywords

    var category by KeywordDao optionalReferencedOn RichSkillDescriptorTable.category

    var collections by CollectionDao via CollectionSkills


    fun canonicalUrl(baseUrl: String): String {
        return "${baseUrl}/api/skills/${this.uuid}"
    }

    override fun toModel(): RichSkillDescriptor {
        val rsd = RichSkillDescriptor(
            id = id.value,
            creationDate = creationDate,
            updateDate = updateDate,
            uuid = uuid,
            name = name,
            statement = statement,
            jobCodes = jobCodes.map { it.toModel() }.sortedBy { it.code },
            keywords = keywords.map { it.toModel() }.sortedBy { it.id!! },
            category = category?.toModel(),
            author = author?.toModel(),
            isExternallyShared = isExternallyShared,
            archiveDate = archiveDate,
            publishDate = publishDate,
            collections = collections.map { it.toModel() }.toList().sortedBy { it.name }
        )
        return rsd
    }
}
