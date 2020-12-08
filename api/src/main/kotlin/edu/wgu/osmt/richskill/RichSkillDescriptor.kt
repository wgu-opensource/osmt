package edu.wgu.osmt.richskill

import edu.wgu.osmt.auditlog.Change
import edu.wgu.osmt.db.*
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import net.minidev.json.JSONObject
import org.valiktor.functions.isEqualTo
import org.valiktor.validate
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.valiktor.functions.validate
import java.util.*
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.jobcode.JobCodeDao
import edu.wgu.osmt.keyword.KeywordDao
import org.valiktor.functions.isNotEqualTo

data class RichSkillDescriptor(
    override val id: Long?,
    override val creationDate: LocalDateTime,
    override val updateDate: LocalDateTime,
    val uuid: String,
    val name: String,
    val statement: String,
    val jobCodes: List<JobCode> = listOf(),
    val keywords: List<Keyword> = listOf(),
    val category: Keyword? = null,
    val author: Keyword? = null,
    override val archiveDate: LocalDateTime? = null,
    override val publishDate: LocalDateTime? = null,
    val collectionIds: Set<Long> = setOf()
) : DatabaseData, HasUpdateDate, PublishStatusDetails {

    // Keyword collections
    val certifications: List<Keyword>
        get() = this.keywords.filter { it.type == KeywordTypeEnum.Certification }

    val standards: List<Keyword>
        get() = this.keywords.filter { it.type == KeywordTypeEnum.Standard }

    val searchingKeywords: List<Keyword>
        get() = this.keywords.filter { it.type == KeywordTypeEnum.Keyword }

    val alignments: List<Keyword>
        get() = this.keywords.filter { it.type == KeywordTypeEnum.Alignment }

    val employers: List<Keyword>
        get() = this.keywords.filter { it.type == KeywordTypeEnum.Employer }

    fun canonicalUrl(baseUrl: String): String = "$baseUrl/api/skills/${uuid}"

    companion object {
        fun create(name: String, statement: String): RichSkillDescriptor {
            val now = LocalDateTime.now(ZoneOffset.UTC)
            return RichSkillDescriptor(
                id = null,
                uuid = UUID.randomUUID().toString(),
                name = name,
                statement = statement,
                creationDate = now,
                updateDate = now
            )
        }
    }
}

data class RsdUpdateObject(
    override val id: Long? = null,
    val name: String? = null,
    val statement: String? = null,
    val author: NullableFieldUpdate<KeywordDao>? = null,
    val category: NullableFieldUpdate<KeywordDao>? = null,
    val keywords: ListFieldUpdate<KeywordDao>? = null,
    val jobCodes: ListFieldUpdate<JobCodeDao>? = null,
    val collections: ListFieldUpdate<CollectionDao>? = null,
    override val publishStatus: PublishStatus? = null
) : UpdateObject<RichSkillDescriptorDao>, HasPublishStatus, Compares<RichSkillDescriptorDao> {

    init {
        validate(this) {
            validate(RsdUpdateObject::category).validate {
                validate(NullableFieldUpdate<KeywordDao>::t).validate {
                    validate(KeywordDao::type).isEqualTo(KeywordTypeEnum.Category)
                }
            }
            validate(RsdUpdateObject::author).validate {
                validate(NullableFieldUpdate<KeywordDao>::t).validate {
                    validate(KeywordDao::type).isEqualTo(KeywordTypeEnum.Author)
                }
            }
        }
    }

    fun compareName(that: RichSkillDescriptorDao): Change? {
        return name?.let {
            change(that::name.name, that.name, it)
        }
    }

    fun compareStatement(that: RichSkillDescriptorDao): Change? {
        return statement?.let {
            change(that::statement.name, that.statement, it)
        }
    }

    fun compareCategory(that: RichSkillDescriptorDao): Change? {
        return category?.let {
            if (that.category?.value?.let { id } != it.t?.id?.value) {
                change(that::category.name, that.category?.let { it.value }, it.t?.value)
            } else null
        }
    }

    fun compareAuthor(that: RichSkillDescriptorDao): Change? {
        return author?.let {
            if (that.author?.value?.let { id } != it.t?.id?.value) {
                change(that::author.name, that.author?.value?.let { it }, it.t?.value)
            } else null
        }
    }

    fun comparePublishStatus(that: RichSkillDescriptorDao): Change? {
        return publishStatus?.let {
            change(that::publishStatus.name, that.publishStatus().name, it.name)
        }
    }

    private fun compareKeyword(that: RichSkillDescriptorDao, keywordTypeEnum: KeywordTypeEnum): Change? {
        val added = keywords?.add?.filter { it.type == keywordTypeEnum }?.mapNotNull { it.value }
        val removed = keywords?.remove?.filter { it.type == keywordTypeEnum }?.mapNotNull { it.value }

        val adjusted = that.keywords.filter { it.type == keywordTypeEnum }.mapNotNull { it.value }.toMutableList()
        removed?.map { removedDao -> adjusted.remove(removedDao) }
        added?.map { addedDao -> adjusted.add(addedDao) }

        val oldKeywords = that.keywords.filter { it.type == keywordTypeEnum }.mapNotNull { it.value }.let{ if (it.isEmpty()){null} else {it} }

        return if (!added.isNullOrEmpty() || !removed.isNullOrEmpty()) {
            change(
                keywordTypeEnum.displayName,
                oldKeywords?.joinToString(delimiter),
                adjusted.joinToString(delimiter)
            )
        } else {
            null
        }
    }

    fun compareSearchingKeywords(that: RichSkillDescriptorDao): Change? {
        return compareKeyword(that, KeywordTypeEnum.Keyword)
    }

    fun compareStandards(that: RichSkillDescriptorDao): Change? {
        return compareKeyword(that, KeywordTypeEnum.Standard)
    }

    fun compareCertifications(that: RichSkillDescriptorDao): Change? {
        return compareKeyword(that, KeywordTypeEnum.Certification)
    }

    fun compareAlignments(that: RichSkillDescriptorDao): Change? {
        return compareKeyword(that, KeywordTypeEnum.Alignment)
    }

    fun compareEmployers(that: RichSkillDescriptorDao): Change? {
        return compareKeyword(that, KeywordTypeEnum.Employer)
    }

    fun compareCollections(that: RichSkillDescriptorDao): Change? {
        val added = collections?.add
        val removed = collections?.remove

        val adjusted = that.collections.toMutableList()
        removed?.map { removedDao -> adjusted.remove(removedDao) }
        added?.map { addedDao -> adjusted.add(addedDao) }

        return if (!added.isNullOrEmpty() || !added.isNullOrEmpty()) {
            change(
                that::collections.name,
                that.collections.map { it.name }.joinToString(delimiter),
                adjusted.map { it.name }.joinToString(delimiter)
            )
        } else {
            null
        }
    }

    override val comparisonList: List<(t: RichSkillDescriptorDao) -> Change?> =
        listOf(
            ::compareName,
            ::compareStatement,
            ::compareCategory,
            ::compareAuthor,
            ::comparePublishStatus,
            ::compareCollections,
            ::compareSearchingKeywords,
            ::compareStandards,
            ::compareCertifications,
            ::compareAlignments,
            ::compareEmployers
        )
}


