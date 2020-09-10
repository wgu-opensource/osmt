package edu.wgu.osmt.collection

import edu.wgu.osmt.db.*
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptor
import net.minidev.json.JSONObject
import org.valiktor.functions.isEqualTo
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
    val skills: List<RichSkillDescriptor> = listOf()
) : DatabaseData, HasUpdateDate {

}

data class CollectionUpdateObject(
    override val id: Long,
    val name: String? = null,
    val author: NullableFieldUpdate<Keyword>? = null,
    val skills: ListFieldUpdate<RichSkillDescriptor>? = null
) : UpdateObject<CollectionDao> {
    init {
        validate(this) {
            validate(CollectionUpdateObject::author).validate {
                validate(NullableFieldUpdate<Keyword>::t).validate {
                    validate(Keyword::type).isEqualTo(KeywordTypeEnum.Author)
                }
            }
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

    override val comparisonList: List<(t: CollectionDao) -> JSONObject?> =
        listOf(::compareName, ::compareAuthor)
}

