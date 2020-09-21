package edu.wgu.osmt.collection

import edu.wgu.osmt.db.*
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptor
import net.minidev.json.JSONObject
import org.valiktor.functions.isEqualTo
import org.valiktor.functions.isNotEqualTo
import org.valiktor.functions.validate
import org.valiktor.validate
import java.time.LocalDateTime
import java.util.*


data class Collection(
    override val id: Long?,
    override val creationDate: LocalDateTime,
    override val updateDate: LocalDateTime,
    val uuid: UUID,
    val name: String,
    val author: Keyword? = null,
    val skills: List<RichSkillDescriptor> = listOf(),
    override val archiveDate: LocalDateTime? = null,
    override val publishDate: LocalDateTime? = null
) : DatabaseData, HasUpdateDate, PublishStatusDetails {

}

data class CollectionUpdateObject(
    override val id: Long,
    val name: String? = null,
    val author: NullableFieldUpdate<Keyword>? = null,
    val skills: ListFieldUpdate<RichSkillDescriptor>? = null,
    override val publishStatus: PublishStatus?
) : UpdateObject<CollectionDao>, HasPublishStatus {
    init {
        validate(this) {
            validate(CollectionUpdateObject::author).validate {
                validate(NullableFieldUpdate<Keyword>::t).validate {
                    validate(Keyword::type).isEqualTo(KeywordTypeEnum.Author)
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
            if (that.author?.let { id } != it.t?.id) {
                jsonUpdateStatement(that.name, that.author?.let { it.value }, it.t?.value)
            } else null
        }
    }

    fun comparePublishStatus(that: CollectionDao): JSONObject?{
        return publishStatus?.let{
            jsonUpdateStatement(::publishStatus.name, that.publishStatus().name, it.name)
        }
    }

    override val comparisonList: List<(t: CollectionDao) -> JSONObject?> =
        listOf(::compareName, ::compareAuthor, ::comparePublishStatus)
}

