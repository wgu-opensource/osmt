package edu.wgu.osmt.richskill

import edu.wgu.osmt.auditlog.*
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.collection.CollectionSkills
import edu.wgu.osmt.db.*
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.jobcode.JobCodeDao
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.nullIfEmpty
import org.valiktor.functions.isEqualTo
import org.valiktor.functions.validate
import org.valiktor.functions.validateForEach
import org.valiktor.validate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.reflect.KProperty1

data class RichSkillDescriptor(
    override val id: Long?,
    override val creationDate: LocalDateTime,
    override val updateDate: LocalDateTime,
    val uuid: String,
    val name: String,
    val statement: String,
    val jobCodes: List<JobCode> = listOf(),
    val keywords: List<Keyword> = listOf(),
    val categories: List<Keyword> = listOf(),
    val authors: List<Keyword> = listOf(),
    override val archiveDate: LocalDateTime? = null,
    override val publishDate: LocalDateTime? = null,
    val collections: List<Collection> = listOf()
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

fun RichSkillDescriptor.diff(old: RichSkillDescriptor?): List<Change> {
    val new = this

    old?.let { if (it.uuid != new.uuid) throw Exception("Tried to compare different UUIDs, ${it.uuid} != ${new.uuid}") }

    val comparisons: List<Comparison<*>> = listOf(
        Comparison(RichSkillDescriptor::name.name, RichSkillDescriptorComparisons::compareName, old, new),
        Comparison(RichSkillDescriptor::statement.name, RichSkillDescriptorComparisons::compareStatement, old, new),
        Comparison(RichSkillDescriptor::categories.name, RichSkillDescriptorComparisons::compareCategories, old, new),
        Comparison(RichSkillDescriptor::authors.name, RichSkillDescriptorComparisons::compareAuthors, old, new),
        Comparison(
            RichSkillDescriptor::publishStatus.name,
            RichSkillDescriptorComparisons::comparePublishStatus,
            old,
            new
        ),
        Comparison(
            RichSkillDescriptor::certifications.name,
            RichSkillDescriptorComparisons::compareCertifications,
            old,
            new
        ),
        Comparison(RichSkillDescriptor::standards.name, RichSkillDescriptorComparisons::compareStandards, old, new),
        Comparison(RichSkillDescriptor::alignments.name, RichSkillDescriptorComparisons::compareAlignments, old, new),
        Comparison(RichSkillDescriptor::employers.name, RichSkillDescriptorComparisons::compareEmployers, old, new),
        Comparison(
            RichSkillDescriptor::searchingKeywords.name,
            RichSkillDescriptorComparisons::compareSearchingKeywords,
            old,
            new
        ),
        Comparison(RichSkillDescriptor::jobCodes.name, RichSkillDescriptorComparisons::compareJobCodes, old, new),
        Comparison(RichSkillDescriptor::collections.name, RichSkillDescriptorComparisons::compareCollections, old, new)
    )

    return comparisons.mapNotNull { it.compare() }
}

data class RsdUpdateObject(
    override val id: Long? = null,
    val name: String? = null,
    val statement: String? = null,
    val authors: ListFieldUpdate<KeywordDao>? = null,
    val categories: ListFieldUpdate<KeywordDao>? = null,
    val keywords: ListFieldUpdate<KeywordDao>? = null,
    val jobCodes: ListFieldUpdate<JobCodeDao>? = null,
    val collections: ListFieldUpdate<CollectionDao>? = null,
    override val publishStatus: PublishStatus? = null
) : UpdateObject<RichSkillDescriptorDao>, HasPublishStatus<RichSkillDescriptorDao> {

    init {
        validate(this) {
            /*validate(RsdUpdateObject::category).validate {
                validate(NullableFieldUpdate<KeywordDao>::t).validate {
                    validate(KeywordDao::type).isEqualTo(KeywordTypeEnum.Category)
                }
            }*/
            validate(RsdUpdateObject::authors).validate {
                validate(ListFieldUpdate<KeywordDao>::add).validateForEach {
                    validate(KeywordDao::type).isEqualTo(KeywordTypeEnum.Author)
                }
                validate(ListFieldUpdate<KeywordDao>::remove).validateForEach {
                    validate(KeywordDao::type).isEqualTo(KeywordTypeEnum.Author)
                }
            }
        }
    }

    override fun applyToDao(dao: RichSkillDescriptorDao): Unit {
        dao.updateDate = LocalDateTime.now(ZoneOffset.UTC)

        applyPublishStatus(dao)
        name?.let { dao.name = it }
        statement?.let { dao.statement = it }
        applyAuthors()
        applyCategories()
        applyKeywords()
        applyJobCodes()
        applyCollections()
    }

    fun applyAuthors(): RsdUpdateObject {
        authors?.let {
            it.add?.forEach { author ->
                RichSkillAuthors.create(richSkillId = id!!, authorId = author.id.value)
            }

            it.remove?.forEach {author ->
                RichSkillAuthors.delete(richSkillId = id!!, authorId = author.id.value)
            }
        }
        return copy(authors = null)
    }

    fun applyCategories(): RsdUpdateObject {
        categories?.let {
            it.add?.forEach { category ->
                RichSkillCategories.create(richSkillId = id!!, categoryId = category.id.value)
            }

            it.remove?.forEach {category ->
                RichSkillCategories.delete(richSkillId = id!!, categoryId = category.id.value)
            }
        }
        return copy(categories = null)
    }

    fun applyKeywords(): RsdUpdateObject {
        keywords?.let {
            it.add?.forEach { keyword ->
                RichSkillKeywords.create(richSkillId = id!!, keywordId = keyword.id.value)
            }

            it.remove?.forEach { keyword ->
                RichSkillKeywords.delete(richSkillId = id!!, keywordId = keyword.id.value)
            }
        }
        return copy(keywords = null)
    }

    fun applyJobCodes(): RsdUpdateObject {
        jobCodes?.let {
            it.add?.forEach { jobCode ->
                RichSkillJobCodes.create(richSkillId = id!!, jobCodeId = jobCode.id.value)
            }
            it.remove?.forEach { jobCode ->
                RichSkillJobCodes.delete(richSkillId = id!!, jobCodeId = jobCode.id.value)
            }
        }
        return copy(jobCodes = null)
    }

    fun applyCollections(): RsdUpdateObject {
        collections?.let {
            it.add?.forEach { collection ->
                CollectionSkills.create(
                    collectionId = collection.id.value,
                    skillId = id!!
                )
            }
            it.remove?.forEach { collection ->
                CollectionSkills.delete(collectionId = collection.id.value, skillId = id!!)
            }
        }
        return copy(collections = null)
    }
}

object RichSkillDescriptorComparisons {
    fun keywordsCompare(
        receiver: RichSkillDescriptor,
        kproperty: KProperty1<RichSkillDescriptor, List<Keyword>>
    ): String? {
        return kproperty.call(receiver).mapNotNull { it.value }.nullIfEmpty()?.joinToString(DELIMITER)
    }

    fun compareName(r: RichSkillDescriptor): String {
        return r.name
    }

    fun compareStatement(r: RichSkillDescriptor): String {
        return r.statement
    }

    fun compareCategories(receiver: RichSkillDescriptor): String? {
        return keywordsCompare(receiver, RichSkillDescriptor::categories)
    }

    fun compareAuthors(receiver: RichSkillDescriptor): String? {
        return keywordsCompare(receiver, RichSkillDescriptor::authors)
    }

    fun comparePublishStatus(r: RichSkillDescriptor): String {
        return r.publishStatus().name
    }

    fun compareCertifications(receiver: RichSkillDescriptor): String? {
        return keywordsCompare(receiver, RichSkillDescriptor::certifications)
    }

    fun compareStandards(receiver: RichSkillDescriptor): String? {
        return keywordsCompare(receiver, RichSkillDescriptor::standards)
    }

    fun compareAlignments(receiver: RichSkillDescriptor): String? {
        return keywordsCompare(receiver, RichSkillDescriptor::alignments)
    }

    fun compareEmployers(receiver: RichSkillDescriptor): String? {
        return keywordsCompare(receiver, RichSkillDescriptor::employers)
    }

    fun compareSearchingKeywords(receiver: RichSkillDescriptor): String? {
        return keywordsCompare(receiver, RichSkillDescriptor::searchingKeywords)
    }

    fun compareJobCodes(r: RichSkillDescriptor): String? {
        return r.jobCodes.mapNotNull { it.code }.nullIfEmpty()?.joinToString(DELIMITER)
    }

    fun compareCollections(r: RichSkillDescriptor): String?{
        return r.collections.mapNotNull{it.name}.nullIfEmpty()?.joinToString(DELIMITER)
    }
}
