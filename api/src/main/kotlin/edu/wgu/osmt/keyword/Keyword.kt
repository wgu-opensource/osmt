package edu.wgu.osmt.keyword

import com.fasterxml.jackson.annotation.JsonView
import edu.wgu.osmt.db.*
import edu.wgu.osmt.richskill.RichSkillView
import net.minidev.json.JSONObject
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

data class Keyword(
    @field:JsonView(RichSkillView.PrivateDetailView::class)
    override val id: Long?,

    @field:JsonView(RichSkillView.PrivateDetailView::class)
    override val creationDate: LocalDateTime,

    @field:JsonView(RichSkillView.PrivateDetailView::class)
    override val updateDate: LocalDateTime,

    @field:JsonView(RichSkillView.PublicDetailView::class)
    val type: KeywordTypeEnum,

    @field:JsonView(RichSkillView.PublicDetailView::class)
    val value: String? = null,

    @field:JsonView(RichSkillView.PublicDetailView::class)
    val uri: String? = null
) : DatabaseData, HasUpdateDate {
}

data class KeywordUpdateObj(override val id: Long, val value: String?, val uri: NullableFieldUpdate<String>?) :
    UpdateObject<Keyword> {

    fun compareValue(that: Keyword): JSONObject? = compare(that::value, this::value, stringOutput)

    fun compareUri(that: Keyword): JSONObject? {
        return uri?.let {
            compare(that::uri, it::t, stringOutput)
        }
    }

    override val comparisonList: List<(t: Keyword) -> JSONObject?> = listOf(::compareValue, ::compareUri)
}

object KeywordTable : LongIdTable("Keyword"), TableWithUpdate<KeywordUpdateObj> {
    override val creationDate = datetime("creationDate")
    override val updateDate = datetime("updateDate")
    val value: Column<String?> = varchar("value", 768).nullable()
    val uri: Column<String?> = varchar("uri", 768).nullable()

    val keyword_type_enum =
        customEnumeration(
            "keyword_type_enum",
            fromDb = { value -> KeywordTypeEnum.valueOf(value as String) }, toDb = { it.name })

    init {
        index(true, keyword_type_enum, value)
        index(true, keyword_type_enum, uri)
    }

    override fun updateBuilderApplyFromUpdateObject(
        updateBuilder: UpdateBuilder<Number>,
        updateObject: KeywordUpdateObj
    ) {
        super.updateBuilderApplyFromUpdateObject(updateBuilder, updateObject)
        updateObject.uri?.let{
            if (it.t == null) updateBuilder[uri] = null
            else {updateBuilder[uri] = it.t}
        }
        updateObject.value?.let{ updateBuilder[value] = it }
    }
}
