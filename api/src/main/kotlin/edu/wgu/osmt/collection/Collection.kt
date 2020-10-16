package edu.wgu.osmt.collection

import edu.wgu.osmt.db.*
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import net.minidev.json.JSONObject
import org.valiktor.functions.isEqualTo
import org.valiktor.functions.isNotEqualTo
import org.valiktor.functions.validate
import org.valiktor.validate
import java.time.LocalDateTime

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
            validate(CollectionUpdateObject::publishStatus).isNotEqualTo(PublishStatus.Unpublished)
        }
    }
    fun compareName(that: CollectionDao): JSONObject? {
        return name?.let {
            compare(that::name, this::name, stringOutput)
        }
    }

    fun compareAuthor(that: CollectionDao): JSONObject? {
        return author?.let {
            if (that.author?.value?.let { id } != it.t?.id?.value) {
                jsonUpdateStatement(that::author.name, that.author?.let { it.value }, it.t?.value)
            } else null
        }
    }

    fun comparePublishStatus(that: CollectionDao): JSONObject?{
        return publishStatus?.let{
            jsonUpdateStatement(::publishStatus.name, that.publishStatus().name, it.name)
        }
    }

    fun compareSkills(that: CollectionDao): JSONObject? {
        val added = skills?.add?.map { mutableMapOf("id" to it.id.value, "name" to it.name) }
        val removed = skills?.remove?.map { mutableMapOf("id" to it.id.value, "name" to it.name) }
        val addedPair = added?.let { "added" to it }
        val removedPair = removed?.let { "removed" to it }
        val operationsList = listOfNotNull(addedPair, removedPair).toTypedArray()

        return if (added?.isNotEmpty() == true or (removed?.isNotEmpty() == true)) {
            JSONObject(mutableMapOf(that::skills.name to mutableMapOf(*operationsList)))
        } else {
            null
        }
    }

    override val comparisonList: List<(t: CollectionDao) -> JSONObject?> =
        listOf(::compareName, ::compareAuthor, ::comparePublishStatus, ::compareSkills)
}

