package edu.wgu.osmt.collection

import edu.wgu.osmt.auditlog.Change
import edu.wgu.osmt.auditlog.Comparison
import edu.wgu.osmt.db.*
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
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
    val author: Keyword? = null,
    override val archiveDate: LocalDateTime? = null,
    override val publishDate: LocalDateTime? = null
) : DatabaseData, HasUpdateDate, PublishStatusDetails {

    fun canonicalUrl(baseUrl: String): String = "$baseUrl/api/collections/${uuid}"
}

data class CollectionUpdateObject(
    override val id: Long? = null,
    val name: String? = null,
    val author: NullableFieldUpdate<KeywordDao>? = null,
    val skills: ListFieldUpdate<RichSkillDescriptorDao>? = null,
    override val publishStatus: PublishStatus? = null
) : UpdateObject<CollectionDao>, HasPublishStatus {
    init {
        validate(this) {
            validate(CollectionUpdateObject::author).validate {
                validate(NullableFieldUpdate<KeywordDao>::t).validate {
                    validate(KeywordDao::type).isEqualTo(KeywordTypeEnum.Author)
                }
            }
        }
    }

    fun applyPublishStatus(dao: CollectionDao): CollectionUpdateObject{
        when (publishStatus) {
            PublishStatus.Archived -> dao.archiveDate = LocalDateTime.now(ZoneOffset.UTC)
            PublishStatus.Published -> dao.publishDate = LocalDateTime.now(ZoneOffset.UTC)
            PublishStatus.Unarchived -> dao.archiveDate = null
            PublishStatus.Deleted -> {
                if (dao.publishDate == null){
                    dao.archiveDate = LocalDateTime.now(ZoneOffset.UTC)
                }
            }
        }
        return copy(publishStatus = null)
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
        Comparison(Collection::author.name, CollectionComparison::compareAuthor, old, new),
        Comparison(Collection::publishStatus.name, CollectionComparison::comparePublishStatus, old, new)
    )

    return comparisons.mapNotNull { it.compare() }
}

object CollectionComparison {
    fun compareName(c: Collection): String {
        return c.name
    }

    fun compareAuthor(c: Collection): String? {
        return c.author?.value
    }

    fun comparePublishStatus(c: Collection): String {
        return c.publishStatus().name
    }
}

