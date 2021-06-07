package edu.wgu.osmt.richskill

import edu.wgu.osmt.api.FormValidationException
import edu.wgu.osmt.api.model.*
import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.auditlog.AuditOperationType
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.collection.CollectionRepository
import edu.wgu.osmt.collection.CollectionSkills
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.*
import edu.wgu.osmt.jobcode.JobCodeDao
import edu.wgu.osmt.jobcode.JobCodeRepository
import edu.wgu.osmt.jobcode.JobCodeTable
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.task.PublishTask
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.select
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
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
    fun findManyByUUIDs(uuids: List<String>): List<RichSkillDescriptorDao>?
    fun create(updateObject: RsdUpdateObject, user: String): RichSkillDescriptorDao?

    fun createFromApi(skillUpdates: List<ApiSkillUpdate>, user: String): List<RichSkillDescriptorDao>
    fun updateFromApi(existingSkillId: Long, skillUpdate: ApiSkillUpdate, user: String): RichSkillDescriptorDao?
    fun rsdUpdateFromApi(skillUpdate: ApiSkillUpdate, user: String): RsdUpdateObject

    fun changeStatusesForTask(task: PublishTask): ApiBatchResult

    fun containingJobCode(jobCode: String): SizedIterable<RichSkillDescriptorDao>
}

@Repository
@Transactional
class RichSkillRepositoryImpl @Autowired constructor(
    val auditLogRepository: AuditLogRepository,
    val collectionRepository: CollectionRepository,
    val richSkillEsRepo: RichSkillEsRepo,
    val collectionEsRepo: CollectionEsRepo,
    val appConfig: AppConfig
) :
    RichSkillRepository {

    @Autowired
    @Lazy
    lateinit var jobCodeRepository: JobCodeRepository

    @Autowired
    @Lazy
    lateinit var keywordRepository: KeywordRepository

    override val dao = RichSkillDescriptorDao.Companion
    override val table = RichSkillDescriptorTable

    override fun findAll() = dao.all()

    override fun findById(id: Long) = dao.findById(id)

    override fun update(updateObject: RsdUpdateObject, user: String): RichSkillDescriptorDao? {
        val daoObject = dao.findById(updateObject.id!!)
        val old = daoObject?.toModel()

        daoObject?.let { updateObject.applyToDao(it) }

        val (publishStatusChanges, otherChanges) = daoObject?.toModel()?.diff(old)
            ?.partition { it.fieldName == RichSkillDescriptor::publishStatus.name } ?: (null to null)

        otherChanges?.let { it ->
            if (it.isNotEmpty())
                auditLogRepository.create(
                    AuditLog.fromAtomicOp(
                        table,
                        updateObject.id,
                        it,
                        user,
                        AuditOperationType.Update
                    )
                )
        }

        publishStatusChanges?.let { it ->
            if (it.isNotEmpty())
                auditLogRepository.create(
                    AuditLog.fromAtomicOp(
                        table,
                        updateObject.id,
                        it,
                        user,
                        AuditOperationType.PublishStatusChange
                    )
                )
        }

        daoObject?.let {
            collectionEsRepo.saveAll(it.collections.map { it.toDoc() })
            richSkillEsRepo.save(RichSkillDoc.fromDao(it, appConfig))
        }
        return daoObject
    }

    override fun findByUUID(uuid: String): RichSkillDescriptorDao? {
        val query = table.select { table.uuid eq uuid }.firstOrNull()
        return query?.let { dao.wrapRow(it) }
    }
    override fun findManyByUUIDs(uuids: List<String>): List<RichSkillDescriptorDao>? {
        val query = table.select { table.uuid inList uuids }
        return query.map { dao.wrapRow(it) }

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
            this.author = updateObject.author?.t
            this.category = updateObject.category?.t
        }

        updateObject.copy(id = newRsd.id.value).applyToDao(newRsd)

        newRsd.let {
            collectionEsRepo.saveAll(it.collections.map { it.toDoc() })
            richSkillEsRepo.save(RichSkillDoc.fromDao(it, appConfig))
        }

        auditLogRepository.create(
            AuditLog.fromAtomicOp(
                table,
                newRsd.id.value,
                newRsd.toModel().diff(null),
                user,
                AuditOperationType.Insert
            )
        )

        return newRsd
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

        fun lookup_alignments(lud: ApiAlignmentListUpdate, keywordType: KeywordTypeEnum) {
            lud.add?.map {
                keywordRepository.findOrCreate(keywordType, value = it.skillName, uri = it.id)
            }?.filterNotNull()?.let {
                addingKeywords.addAll(it)
            }

            lud.remove?.map {
                keywordRepository.findByValueOrUri(keywordType, value = it.skillName, uri = it.id)
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
            it.add?.filter { it.isNotBlank() }?.map {
                jobCodeRepository.findByCodeOrCreate(code = it)
            }?.let { jobsToAdd.addAll(it) }

            it.remove?.filter { it.isNotBlank() }?.map {
                jobCodeRepository.findByCode(it)
            }?.let { jobsToRemove.addAll(it.filterNotNull()) }
        }

        skillUpdate.keywords?.let { lookup_keywords(it, KeywordTypeEnum.Keyword) }
        skillUpdate.certifications?.let { lookup_references(it, KeywordTypeEnum.Certification) }
        skillUpdate.standards?.let { lookup_references(it, KeywordTypeEnum.Standard) }
        skillUpdate.alignments?.let { lookup_alignments(it, KeywordTypeEnum.Alignment) }
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

        val publish_skill = { skillDao: RichSkillDescriptorDao, task: PublishTask ->
            val oldStatus = skillDao.publishStatus()
            if (oldStatus != task.publishStatus) {
                val updateObj = RsdUpdateObject(id = skillDao.id.value, publishStatus = task.publishStatus)
                val updatedDao = this.update(updateObj, task.userString)
                val newStatus = updatedDao?.publishStatus()
                (newStatus != oldStatus)
            } else false

        }

        val handle_skill_dao = { skillDao: RichSkillDescriptorDao? ->
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
            val searchHits = richSkillEsRepo.byApiSearch(
                publishTask.search,
                publishTask.filterByStatus,
                Pageable.unpaged(),
                publishTask.collectionUuid
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

    override fun containingJobCode(jobCode: String): SizedIterable<RichSkillDescriptorDao> {
        val query = RichSkillDescriptorTable.innerJoin(RichSkillJobCodes).innerJoin(JobCodeTable)
            .slice(RichSkillDescriptorTable.columns).select {
            JobCodeTable.code eq jobCode
        }
        return RichSkillDescriptorDao.wrapRows(query)
    }
}

