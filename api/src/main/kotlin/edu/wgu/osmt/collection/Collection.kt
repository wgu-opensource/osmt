package edu.wgu.osmt.collection

import edu.wgu.osmt.auditlog.Change
import edu.wgu.osmt.auditlog.Comparison
import edu.wgu.osmt.db.*
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import org.apache.commons.lang3.StringUtils
import org.valiktor.functions.isEqualTo
import org.valiktor.functions.validate
import org.valiktor.validate
import java.time.LocalDateTime
import java.time.ZoneOffset

data class Collection(
    override val id: Long?,
    override val creationDate: LocalDateTime,
    override val updateDate: LocalDateTime,
    val uuid: String,
    val name: String,
    val description: String? = null,
    val author: Keyword? = null,
    val workspaceOwner: String? = null,
    val status: PublishStatus,
    override val archiveDate: LocalDateTime? = null,
    override val publishDate: LocalDateTime? = null
) : DatabaseData, HasUpdateDate, PublishStatusDetails {

    fun canonicalUrl(baseUrl: String): String = "$baseUrl/api/collections/${uuid}"

    fun isWorkspace() : Boolean {
        return  (this.status == PublishStatus.Workspace && StringUtils.isNotEmpty(this.workspaceOwner))
    }
}

data class CollectionUpdateObject(
    override val id: Long? = null,
    val name: String? = null,
    val description: NullableFieldUpdate<String>? = null,
    val author: NullableFieldUpdate<KeywordDao>? = null,
    val skills: ListFieldUpdate<RichSkillDescriptorDao>? = null,
    override val publishStatus: PublishStatus? = null
) : UpdateObject<CollectionDao>, HasPublishStatus<CollectionDao> {
    init {
        validate(this) {
            validate(CollectionUpdateObject::author).validate {
                validate(NullableFieldUpdate<KeywordDao>::t).validate {
                    validate(KeywordDao::type).isEqualTo(KeywordTypeEnum.Author)
                }
            }
        }
    }

    override fun applyToDao(dao: CollectionDao): Unit{
        dao.updateDate = LocalDateTime.now(ZoneOffset.UTC)
        applyStatusChange(dao)
        name?.let { dao.name = it }

        description?.let {
            if (it.t != null) {
                dao.description = it.t
            } else {
                dao.description = null
            }
        }

        author?.let {
            if (it.t != null) {
                dao.author = it.t
            } else {
                dao.author = null
            }
        }
        applySkills()
    }

    fun applyStatusChange(dao: CollectionDao) {
        when (publishStatus) {
            PublishStatus.Archived ->  {
                dao.archiveDate = LocalDateTime.now(ZoneOffset.UTC)
                dao.status = PublishStatus.Archived
            }
            PublishStatus.Published -> {
                dao.publishDate = LocalDateTime.now(ZoneOffset.UTC)
                dao.status = PublishStatus.Published
                dao.archiveDate = null
            }
            PublishStatus.Unarchived -> {
                if (dao.publishDate != null) {
                    dao.status = PublishStatus.Published

                } else {
                    dao.status = PublishStatus.Draft
                }
                dao.archiveDate = null
            }
            PublishStatus.Draft -> {
                dao.status = PublishStatus.Draft
                dao.archiveDate = null
                dao.publishDate = null

            }
            PublishStatus.Workspace -> {
                dao.status = PublishStatus.Workspace
                dao.archiveDate = null
                dao.publishDate = null
            }
            else -> {}
        }
    }

    fun applySkills(): CollectionUpdateObject{
        skills?.let {
            it.add?.forEach { skill ->
                CollectionSkills.create(collectionId = id!!, skillId = skill.id.value)
            }
            it.remove?.forEach { skill ->
                CollectionSkills.delete(collectionId = id!!, skillId = skill.id.value)
            }
        }
        return copy(skills = null)
    }
}

fun Collection.diff(old: Collection?): List<Change> {
    val new = this

    old?.let{if (it.uuid != new.uuid) throw Exception("Tried to compare different UUIDs, ${it.uuid} != ${new.uuid}")}

    val comparisons: List<Comparison<*>> = listOf(
        Comparison(Collection::name.name, CollectionComparison::compareName, old, new),
        Comparison(Collection::description.name, CollectionComparison::compareDescription, old, new),
        Comparison(Collection::author.name, CollectionComparison::compareAuthor, old, new),
        Comparison(Collection::publishStatus.name, CollectionComparison::comparePublishStatus, old, new)
    )

    return comparisons.mapNotNull { it.compare() }
}

object CollectionComparison {
    fun compareName(c: Collection): String {
        return c.name
    }

    fun compareDescription(c: Collection): String? {
        return c.description
    }

    fun compareAuthor(c: Collection): String? {
        return c.author?.value
    }

    fun comparePublishStatus(c: Collection): String {
        return c.publishStatus().name
    }
}

