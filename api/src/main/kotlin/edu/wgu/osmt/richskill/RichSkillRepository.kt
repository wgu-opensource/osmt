package edu.wgu.osmt.richskill

import edu.wgu.osmt.api.FormValidationException
import edu.wgu.osmt.api.model.ApiBatchResult
import edu.wgu.osmt.api.model.ApiReferenceListUpdate
import edu.wgu.osmt.api.model.ApiSkillUpdate
import edu.wgu.osmt.api.model.ApiStringListUpdate
import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.auditlog.AuditOperationType
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.collection.CollectionRepository
import edu.wgu.osmt.collection.CollectionSkills
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.*
import edu.wgu.osmt.elasticsearch.EsCollectionRepository
import edu.wgu.osmt.elasticsearch.EsRichSkillRepository
import edu.wgu.osmt.elasticsearch.SearchService
import edu.wgu.osmt.jobcode.JobCodeDao
import edu.wgu.osmt.jobcode.JobCodeRepository
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.task.PublishTask
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.select
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

interface RichSkillRepository : PaginationHelpers<RichSkillDescriptorTable> {
    override val table: RichSkillDescriptorTable
    val dao: RichSkillDescriptorDao.Companion

    fun update(updateObject: RsdUpdateObject, user: String): RichSkillDescriptorDao?
    fun findAll(): SizedIterable<RichSkillDescriptorDao>
    fun findById(id: Long): RichSkillDescriptorDao?
    fun findByUUID(uuid: String): RichSkillDescriptorDao?
    fun create(updateObject: RsdUpdateObject, user: String): RichSkillDescriptorDao?

    fun createFromApi(skillUpdates: List<ApiSkillUpdate>, user: String): List<RichSkillDescriptorDao>
    fun updateFromApi(existingSkillId: Long, skillUpdate: ApiSkillUpdate, user: String): RichSkillDescriptorDao?
    fun rsdUpdateFromApi(skillUpdate: ApiSkillUpdate, user: String): RsdUpdateObject

    fun changeStatusesForTask(task: PublishTask): ApiBatchResult
}

@Repository
@Transactional
class RichSkillRepositoryImpl @Autowired constructor(
    val auditLogRepository: AuditLogRepository,
    val jobCodeRepository: JobCodeRepository,
    val keywordRepository: KeywordRepository,
    val collectionRepository: CollectionRepository,
    val esRichSkillRepository: EsRichSkillRepository,
    val esCollectionRepository: EsCollectionRepository,
    val searchService: SearchService,
    val appConfig: AppConfig
) :
    RichSkillRepository {
    override val dao = RichSkillDescriptorDao.Companion
    override val table = RichSkillDescriptorTable

    val richSkillKeywordTable = RichSkillKeywords
    val richSkillJobCodeTable = RichSkillJobCodes
    val richSkillCollectionTable = CollectionSkills


    override fun findAll() = dao.all()

    override fun findById(id: Long) = dao.findById(id)

    fun applyUpdate(rsdDao: RichSkillDescriptorDao, updateObject: RsdUpdateObject): Unit {
        rsdDao.updateDate = LocalDateTime.now(ZoneOffset.UTC)
        when (updateObject.publishStatus) {
            PublishStatus.Archived -> {
                if (rsdDao.publishDate != null) {
                    rsdDao.archiveDate = LocalDateTime.now(ZoneOffset.UTC)
                }
            }
            PublishStatus.Published -> {
                if (rsdDao.archiveDate != null) {
                    rsdDao.archiveDate = null // unarchive
                } else {
                    rsdDao.publishDate = LocalDateTime.now(ZoneOffset.UTC)
                }
            }
            PublishStatus.Unpublished -> {
            } // non-op
        }
        updateObject.name?.let { rsdDao.name = it }
        updateObject.statement?.let { rsdDao.statement = it }
        updateObject.category?.let {
            if (it.t != null) {
                rsdDao.category = it.t
            } else {
                rsdDao.category = null
            }
        }
        updateObject.author?.let {
            if (it.t != null) {
                rsdDao.author = it.t
            } else {
                rsdDao.author = null
            }
        }

        // update keywords
        updateObject.keywords?.let {

            it.add?.forEach { keyword ->
                richSkillKeywordTable.create(richSkillId = updateObject.id!!, keywordId = keyword.id.value)
            }

            it.remove?.forEach { keyword ->
                richSkillKeywordTable.delete(richSkillId = updateObject.id!!, keywordId = keyword.id.value)
            }
        }

        // update jobcodes
        updateObject.jobCodes?.let {
            it.add?.forEach { jobCode ->
                richSkillJobCodeTable.create(richSkillId = updateObject.id!!, jobCodeId = jobCode.id.value)
            }
            it.remove?.forEach { jobCode ->
                richSkillJobCodeTable.delete(richSkillId = updateObject.id!!, jobCodeId = jobCode.id.value!!)
            }
        }

        // update collections
        updateObject.collections?.let {
            it.add?.forEach { collection ->
                richSkillCollectionTable.create(
                    collectionId = collection.id.value!!,
                    skillId = updateObject.id!!
                )
            }
            it.remove?.forEach { collection ->
                richSkillCollectionTable.delete(collectionId = collection.id.value!!, skillId = updateObject.id!!)
            }
        }
    }

    override fun update(updateObject: RsdUpdateObject, user: String): RichSkillDescriptorDao? {
        val daoObject = dao.findById(updateObject.id!!)
        val changes = daoObject?.let { updateObject.diff(it) }

        daoObject?.let { applyUpdate(it, updateObject) }

        changes?.let { it ->
            if (it.isNotEmpty())
                auditLogRepository.insert(
                    AuditLog.fromAtomicOp(
                        table,
                        updateObject.id,
                        it.toString(),
                        user,
                        AuditOperationType.Update
                    )
                )
        }
        daoObject?.let {
            esCollectionRepository.saveAll(it.collections.map { it.toDoc() })
            esRichSkillRepository.save(RichSkillDoc.fromDao(it, appConfig))
        }
        return daoObject
    }

    override fun findByUUID(uuid: String): RichSkillDescriptorDao? {
        val query = table.select { table.uuid eq uuid }.firstOrNull()
        return query?.let { dao.wrapRow(it) }
    }

    override fun create(updateObject: RsdUpdateObject, user: String): RichSkillDescriptorDao? {
        if (updateObject.name.isNullOrBlank() || updateObject.statement.isNullOrBlank()) {
            return null
        }

        val newRsd = dao.new {
            this.name = updateObject.name
            this.statement = updateObject.statement
            this.updateDate = LocalDateTime.now(ZoneOffset.UTC)
            this.creationDate = LocalDateTime.now(ZoneOffset.UTC)
            this.uuid = UUID.randomUUID().toString()
        }

        val updateWithIdAndAuthor = updateObject.copy(
            id = newRsd.id.value
        )

        return update(updateWithIdAndAuthor, user)
    }

    override fun createFromApi(skillUpdates: List<ApiSkillUpdate>, user: String): List<RichSkillDescriptorDao> {
        // pre validate all rows
        val allErrors = skillUpdates.mapIndexed { i, updateDto ->
            updateDto.validateForCreation(i)
        }.filterNotNull().flatten()
        if (allErrors.isNotEmpty()) {
            throw FormValidationException("Invalid SkillUpdateDescriptor", allErrors)
        }

        // create records
        val newSkills = skillUpdates.map { update ->
            val rsdUpdateObject = rsdUpdateFromApi(update, user)
            create(rsdUpdateObject, user)
        }
        return newSkills.filterNotNull()
    }

    override fun updateFromApi(
        existingSkillId: Long,
        skillUpdate: ApiSkillUpdate,
        user: String
    ): RichSkillDescriptorDao? {
        val errors = skillUpdate.validate(0)
        if (errors?.isNotEmpty() == true) {
            throw FormValidationException("Invalid SkillUpdateDescriptor", errors)
        }

        val rsdUpdateObject = rsdUpdateFromApi(skillUpdate, user)
        val updateObjectWithId = rsdUpdateObject.copy(
            id = existingSkillId
        )
        return update(updateObjectWithId, user)
    }

    override fun rsdUpdateFromApi(skillUpdate: ApiSkillUpdate, user: String): RsdUpdateObject {
        val authorKeyword = skillUpdate.author?.let {
            keywordRepository.findOrCreate(KeywordTypeEnum.Author, value = it.name, uri = it.id)
        }

        val categoryKeyword = skillUpdate.category?.let {
            keywordRepository.findOrCreate(KeywordTypeEnum.Category, value = it)
        }


        val addingCollections = mutableListOf<CollectionDao>()
        val removingCollections = mutableListOf<CollectionDao>()
        val addingKeywords = mutableListOf<KeywordDao>()
        val removingKeywords = mutableListOf<KeywordDao>()
        val jobsToAdd = mutableListOf<JobCodeDao>()
        val jobsToRemove = mutableListOf<JobCodeDao>()

        skillUpdate.collections?.let { slu ->
            slu.add?.mapNotNull {
                collectionRepository.findByName(it) ?: collectionRepository.create(it, user)
            }?.let {
                addingCollections.addAll(it)
            }

            slu.remove?.mapNotNull {
                collectionRepository.findByName(it) ?: collectionRepository.create(it, user)
            }?.let {
                removingCollections.addAll(it)
            }
        }

        fun lookup_references(lud: ApiReferenceListUpdate, keywordType: KeywordTypeEnum) {
            lud.add?.map {
                keywordRepository.findOrCreate(keywordType, value = it.name, uri = it.id)
            }?.filterNotNull()?.let {
                addingKeywords.addAll(it)
            }

            lud.remove?.map {
                keywordRepository.findByValueOrUri(keywordType, value = it.name, uri = it.id)
            }?.let {
                removingKeywords.addAll(it.filterNotNull())
            }
        }

        fun lookup_keywords(slud: ApiStringListUpdate, keywordType: KeywordTypeEnum) {
            slud.add?.map {
                keywordRepository.findOrCreate(keywordType, value = it)
            }?.filterNotNull()?.let {
                addingKeywords.addAll(it)
            }

            slud.remove?.map {
                keywordRepository.findByValueOrUri(keywordType, value = it)
            }?.let {
                removingKeywords.addAll(it.filterNotNull())
            }
        }

        skillUpdate.occupations?.let {
            it.add?.map {
                jobCodeRepository.findByCodeOrCreate(code = it)
            }?.let { jobsToAdd.addAll(it) }

            it.remove?.map {
                jobCodeRepository.findByCode(it)
            }?.let { jobsToRemove.addAll(it.filterNotNull()) }
        }

        skillUpdate.keywords?.let { lookup_keywords(it, KeywordTypeEnum.Keyword) }
        skillUpdate.certifications?.let { lookup_references(it, KeywordTypeEnum.Certification) }
        skillUpdate.standards?.let { lookup_references(it, KeywordTypeEnum.Standard) }
        skillUpdate.alignments?.let { lookup_references(it, KeywordTypeEnum.Alignment) }
        skillUpdate.employers?.let { lookup_references(it, KeywordTypeEnum.Employer) }

        val allKeywordsUpdate = if (addingKeywords.size > 0 || removingKeywords.size > 0) {
            ListFieldUpdate<KeywordDao>(
                add = if (addingKeywords.size > 0) addingKeywords else listOf(),
                remove = if (removingKeywords.size > 0) removingKeywords else listOf()
            )
        } else {
            null
        }

        val jobCodesUpdate = if (jobsToAdd.size > 0 || jobsToRemove.size > 0) {
            ListFieldUpdate<JobCodeDao>(
                add = if (jobsToAdd.size > 0) jobsToAdd else listOf(),
                remove = if (jobsToRemove.size > 0) jobsToRemove else listOf()
            )
        } else {
            null
        }


        return RsdUpdateObject(
            name = skillUpdate.skillName,
            statement = skillUpdate.skillStatement,
            publishStatus = skillUpdate.publishStatus,
            author = authorKeyword?.let { NullableFieldUpdate(it) },
            category = if (skillUpdate.category != null || skillUpdate.category?.isBlank() == true) NullableFieldUpdate(
                categoryKeyword
            ) else null,
            keywords = allKeywordsUpdate,
            jobCodes = jobCodesUpdate,
            collections = if (addingCollections.size + removingCollections.size > 0) ListFieldUpdate(
                addingCollections,
                removingCollections
            ) else null
        )
    }

    override fun changeStatusesForTask(publishTask: PublishTask): ApiBatchResult {
        var modifiedCount = 0
        var totalCount = 0

        val publish_skill = {skillDao: RichSkillDescriptorDao, task: PublishTask ->
            val oldStatus = skillDao.publishStatus()
            if (oldStatus != task.publishStatus) {
                val updateObj = RsdUpdateObject(id = skillDao.id.value, publishStatus = task.publishStatus)
                val updatedDao = this.update(updateObj, task.userString)
                val newStatus = updatedDao?.publishStatus()
                (newStatus != oldStatus)
            } else false

        }

        val handle_skill_dao = {skillDao: RichSkillDescriptorDao? ->
            skillDao?.let {
                if (publish_skill(it, publishTask)) {
                    modifiedCount += 1
                }
            }
        }

        if (!publishTask.search.uuids.isNullOrEmpty()) {
            totalCount = publishTask.search.uuids.size
            publishTask.search.uuids.forEach { uuid ->
                handle_skill_dao(this.findByUUID(uuid))
            }
        } else {
            val searchHits = searchService.searchRichSkillsByApiSearch(
                publishTask.search,
                publishTask.filterByStatus,
                Pageable.unpaged()
            )
            totalCount = searchHits.totalHits.toInt()
            searchHits.forEach { hit ->
                handle_skill_dao(this.findById(hit.content.id))
            }
        }

        return ApiBatchResult(
            success = true,
            modifiedCount = modifiedCount,
            totalCount = totalCount
        )
    }

}

