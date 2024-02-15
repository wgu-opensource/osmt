package edu.wgu.osmt.richskill

import edu.wgu.osmt.api.model.ApiAlignment
import edu.wgu.osmt.api.model.ApiNamedReference
import edu.wgu.osmt.auditlog.Change
import edu.wgu.osmt.auditlog.Comparison
import edu.wgu.osmt.auditlog.DELIMITER
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.collection.CollectionSkills
import edu.wgu.osmt.db.*
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.jobcode.JobCodeDao
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordCount
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.nullIfEmpty
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.reflect.KProperty1

open class RichSkillDescriptor(
        override val id: Long?,
        override val creationDate: LocalDateTime,
        override val updateDate: LocalDateTime,
        open val uuid: String,
        open val name: String,
        open val statement: String,
        open val jobCodes: List<JobCode> = listOf(),
        open val keywords: List<Keyword> = listOf(),
        override val archiveDate: LocalDateTime? = null,
        override val publishDate: LocalDateTime? = null,
        open val collections: List<Collection> = listOf()
) : DatabaseData, HasUpdateDate, PublishStatusDetails {

    // Keyword collections
    val authors: List<Keyword>
        get() = this.keywords.filter { it.type == KeywordTypeEnum.Author }

    val categories: List<Keyword>
        get() = this.keywords.filter { it.type == KeywordTypeEnum.Category }

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

        fun getKeywordsFromSkills(skills: Iterable<RichSkillDescriptor>): Map<KeywordTypeEnum, List<KeywordCount>> {
            val alignments = skills.flatMap { it.alignments }
                .map { ApiAlignment.fromKeyword(it) }
                .sortedBy{ it.id }

            val authors = skills.flatMap { it.authors }
                .mapNotNull { it.value }
                .sortedBy{ it }

            val categories = skills.flatMap { it.categories }
                .mapNotNull { it.value }
                .sortedBy { it }

            val certifications = skills.flatMap { it.certifications }
                .map { ApiNamedReference.fromKeyword(it) }
                .sortedBy { it.name }

            val employers = skills.flatMap { it.employers }
                .map { ApiNamedReference.fromKeyword(it) }
                .sortedBy { it.name }

            val keywords = skills.flatMap { it.searchingKeywords }
                .mapNotNull { it.value }
                .sortedBy { it }

            val standards = skills.flatMap { it.standards }
                .map { ApiAlignment.fromKeyword(it) }
                .sortedBy { it.id }

            return mapOf(
                KeywordTypeEnum.Alignment to
                    alignments.distinct().map { k ->  KeywordCount(k, alignments.count { it == k }) },
                KeywordTypeEnum.Author to
                    authors.distinct().map { k ->  KeywordCount(k, authors.count { it == k }) },
                KeywordTypeEnum.Category to
                    categories.distinct().map { k ->  KeywordCount(k, categories.count { it == k }) },
                KeywordTypeEnum.Certification to
                    certifications.distinct().map { k ->  KeywordCount(k, certifications.count { it == k }) },
                KeywordTypeEnum.Employer to
                    employers.distinct().map { k ->  KeywordCount(k, employers.count { it == k }) },
                KeywordTypeEnum.Keyword to
                    keywords.distinct().map { k ->  KeywordCount(k, keywords.count { it == k }) },
                KeywordTypeEnum.Standard to
                    standards.distinct().map { k ->  KeywordCount(k, standards.count { it == k }) },
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
    val keywords: ListFieldUpdate<KeywordDao>? = null,
    val jobCodes: ListFieldUpdate<JobCodeDao>? = null,
    val collections: ListFieldUpdate<CollectionDao>? = null,
    override val publishStatus: PublishStatus? = null
) : UpdateObject<RichSkillDescriptorDao>, HasPublishStatus<RichSkillDescriptorDao> {

    override fun applyToDao(dao: RichSkillDescriptorDao): Unit {
        dao.updateDate = LocalDateTime.now(ZoneOffset.UTC)

        applyPublishStatus(dao)
        name?.let { dao.name = it }
        statement?.let { dao.statement = it }
        applyKeywords()
        applyJobCodes()
        applyCollections()
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
