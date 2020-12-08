package edu.wgu.osmt.collection

import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.Change
import edu.wgu.osmt.db.*
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import net.minidev.json.JSONObject
import org.valiktor.functions.isEqualTo
import org.valiktor.functions.validate
import org.valiktor.validate
import java.time.LocalDateTime
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

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
) : UpdateObject<CollectionDao>, HasPublishStatus, Compares<CollectionDao> {
    init {
        validate(this) {
            validate(CollectionUpdateObject::author).validate {
                validate(NullableFieldUpdate<KeywordDao>::t).validate {
                    validate(KeywordDao::type).isEqualTo(KeywordTypeEnum.Author)
                }
            }
        }
    }

    fun compareName(that: CollectionDao): Change? {
        return name?.let {
            change(that::name.name, that.name, it)
        }
    }

    fun compareAuthor(that: CollectionDao): Change? {
        return author?.let {
            if (that.author?.value?.let { id } != it.t?.id?.value) {
                change(that::author.name, that.author?.let { it.value }, it.t?.value)
            } else null
        }
    }

    fun comparePublishStatus(that: CollectionDao): Change?{
        return publishStatus?.let{
            change(::publishStatus.name, that.publishStatus().name, it.name)
        }
    }

    override val comparisonList: List<(t: CollectionDao) -> Change?> =
        listOf(::compareName, ::compareAuthor, ::comparePublishStatus)
}

