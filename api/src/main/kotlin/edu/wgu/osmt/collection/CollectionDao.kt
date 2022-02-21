package edu.wgu.osmt.collection

import edu.wgu.osmt.db.MutablePublishStatusDetails
import edu.wgu.osmt.db.OutputsModel
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import edu.wgu.osmt.richskill.RichSkillDescriptorTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime

class CollectionDao(id: EntityID<Long>) : LongEntity(id), OutputsModel<Collection>, MutablePublishStatusDetails {
    companion object : LongEntityClass<CollectionDao>(CollectionTable)

    var creationDate by CollectionTable.creationDate
    var updateDate: LocalDateTime by CollectionTable.updateDate
    var uuid: String by CollectionTable.uuid
    var name: String by CollectionTable.name
    var author by KeywordDao optionalReferencedOn CollectionTable.author
    var isExternallyShared: Boolean by CollectionTable.externallyShared
    var importedFrom: String? by RichSkillDescriptorTable.importedFrom
    var libraryName: String? by RichSkillDescriptorTable.libraryName

    var skills by RichSkillDescriptorDao via CollectionSkills

    override var publishDate: LocalDateTime? by CollectionTable.publishDate
    override var archiveDate: LocalDateTime? by CollectionTable.archiveDate

    fun canonicalUrl(baseUrl: String): String {
        return "${baseUrl}/api/collections/${this.uuid}"
    }

    override fun toModel(): Collection {
        return Collection(
            id = id.value,
            creationDate = creationDate,
            updateDate = updateDate,
            uuid = uuid,
            name = name,
            author = author?.toModel(),
            isExternallyShared = isExternallyShared,
            archiveDate = archiveDate,
            publishDate = publishDate
        )
    }

    fun toDoc(embedded: Boolean = false): CollectionDoc {
        return CollectionDoc(
            id = id.value,
            uuid = uuid,
            name = name,
            publishStatus = publishStatus(),
            skillIds = if (embedded) null else skills.map { it.uuid },
            skillCount = if (embedded) null else skills.count().toInt(),
            author = author?.value,
            archiveDate = archiveDate,
            publishDate = publishDate
        )
    }
}
