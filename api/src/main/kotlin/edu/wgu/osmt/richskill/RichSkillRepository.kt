package edu.wgu.osmt.richskill

import edu.wgu.osmt.api.FormValidationException
import edu.wgu.osmt.api.model.ApiAlignmentListUpdate
import edu.wgu.osmt.api.model.ApiBatchResult
import edu.wgu.osmt.api.model.ApiReferenceListUpdate
import edu.wgu.osmt.api.model.ApiSkillUpdate
import edu.wgu.osmt.api.model.ApiStringListUpdate
import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.auditlog.AuditOperationType
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.collection.CollectionRepository
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.db.PaginationHelpers
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

    fun createFromApi(skillUpdates: List<ApiSkillUpdate>, user: String, userEmail: String): List<RichSkillDescriptorDao>
    fun updateFromApi(existingSkillId: Long, skillUpdate: ApiSkillUpdate, user: String, email: String): RichSkillDescriptorDao?
    fun rsdUpdateFromApi(skillUpdate: ApiSkillUpdate, user: String, userEmail: String): RsdUpdateObject

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

    override fun createFromApi(skillUpdates: List<ApiSkillUpdate>, user: String, userEmail: String): List<RichSkillDescriptorDao> {
        // pre validate all rows
        val allErrors = skillUpdates.mapIndexed { i, updateDto ->
            updateDto.validateForCreation(i)
        }.filterNotNull().flatten()
        if (allErrors.isNotEmpty()) {
            throw FormValidationException("Invalid SkillUpdateDescriptor", allErrors)
        }

        // create records
        val newSkills = skillUpdates.map { update ->
            val rsdUpdateObject = rsdUpdateFromApi(update, user, userEmail)
            create(rsdUpdateObject, user)
        }
        return newSkills.filterNotNull()
    }

    override fun updateFromApi(
        existingSkillId: Long,
        skillUpdate: ApiSkillUpdate,
        user: String,
        email: String
    ): RichSkillDescriptorDao? {
        val errors = skillUpdate.validate(0)
        if (errors?.isNotEmpty() == true) {
            throw FormValidationException("Invalid SkillUpdateDescriptor", errors)
        }

        val rsdUpdateObject = rsdUpdateFromApi(skillUpdate, user, email)
        val updateObjectWithId = rsdUpdateObject.copy(
            id = existingSkillId
        )
        return update(updateObjectWithId, user)
    }

    override fun rsdUpdateFromApi(skillUpdate: ApiSkillUpdate, user: String, email: String): RsdUpdateObject {
        val addingCollections = mutableListOf<CollectionDao>()
        val removingCollections = mutableListOf<CollectionDao>()
        val addingAuthors = mutableListOf<KeywordDao>()
        val removingAuthors = mutableListOf<KeywordDao>()
        val addingCategories = mutableListOf<KeywordDao>()
        val removingCategories = mutableListOf<KeywordDao>()
        val addingKeywords = mutableListOf<KeywordDao>()
        val removingKeywords = mutableListOf<KeywordDao>()
        val jobsToAdd = mutableListOf<JobCodeDao>()
        val jobsToRemove = mutableListOf<JobCodeDao>()

        skillUpdate.collections?.let { slu ->
            slu.add?.mapNotNull {
                collectionRepository.findByName(it) ?: collectionRepository.create(it, user, email)
            }?.let {
                addingCollections.addAll(it)
            }

            slu.remove?.mapNotNull {
                collectionRepository.findByName(it) ?: collectionRepository.create(it, user, email)
            }?.let {
                removingCollections.addAll(it)
            }
        }

        fun lookupAuthors(lud: ApiStringListUpdate, keywordType: KeywordTypeEnum) {
            lud.add?.map {
                keywordRepository.findOrCreate(keywordType, value = it)
            }?.filterNotNull()?.let {
                addingAuthors.addAll(it)
            }

            lud.remove?.map {
                keywordRepository.findByValueOrUri(keywordType, value = it)
            }?.let {
                removingAuthors.addAll(it.filterNotNull())
            }
        }

        fun lookupCategories(lud: ApiStringListUpdate, keywordType: KeywordTypeEnum) {
            lud.add?.map {
                keywordRepository.findOrCreate(keywordType, value = it)
            }?.filterNotNull()?.let {
                addingCategories.addAll(it)
            }

            lud.remove?.map {
                keywordRepository.findByValueOrUri(keywordType, value = it)
            }?.let {
                removingCategories.addAll(it.filterNotNull())
            }
        }

        fun lookupReferences(lud: ApiReferenceListUpdate, keywordType: KeywordTypeEnum) {
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

        fun lookupAlignments(lud: ApiAlignmentListUpdate, keywordType: KeywordTypeEnum) {
            lud.add?.map {
                keywordRepository.findOrCreate(keywordType, value = it.skillName, uri = it.id, framework = it.isPartOf?.name)
            }?.filterNotNull()?.let {
                addingKeywords.addAll(it)
            }

            lud.remove?.map {
                keywordRepository.findByValueOrUri(keywordType, value = it.skillName, uri = it.id, framework = it.isPartOf?.name)
            }?.let {
                removingKeywords.addAll(it.filterNotNull())
            }
        }

        fun lookupKeywords(slud: ApiStringListUpdate, keywordType: KeywordTypeEnum) {
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

        skillUpdate.authors?.let { lookupAuthors(it, KeywordTypeEnum.Author) }
        skillUpdate.categories?.let { lookupCategories(it, KeywordTypeEnum.Category) }
        skillUpdate.keywords?.let { lookupKeywords(it, KeywordTypeEnum.Keyword) }
        skillUpdate.certifications?.let { lookupReferences(it, KeywordTypeEnum.Certification) }
        skillUpdate.standards?.let { lookupAlignments(it, KeywordTypeEnum.Standard) }
        skillUpdate.alignments?.let { lookupAlignments(it, KeywordTypeEnum.Alignment) }
        skillUpdate.employers?.let { lookupReferences(it, KeywordTypeEnum.Employer) }

        val authorsUpdate = if (addingAuthors.size > 0 || removingAuthors.size > 0) {
            ListFieldUpdate<KeywordDao>(
                add = if (addingAuthors.size > 0) addingAuthors else listOf(),
                remove = if (removingAuthors.size > 0) removingAuthors else listOf()
            )
        } else {
            null
        }

        val categoriesUpdate = if (addingCategories.size > 0 || removingCategories.size > 0) {
            ListFieldUpdate<KeywordDao>(
                add = if (addingCategories.size > 0) addingCategories else listOf(),
                remove = if (removingCategories.size > 0) removingCategories else listOf()
            )
        } else {
            null
        }

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
            authors = authorsUpdate,
            categories = categoriesUpdate,
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

        val publishSkill = { skillDao: RichSkillDescriptorDao, task: PublishTask ->
            val oldStatus = skillDao.publishStatus()
            if (oldStatus != task.publishStatus) {
                val updateObj = RsdUpdateObject(id = skillDao.id.value, publishStatus = task.publishStatus)
                val updatedDao = this.update(updateObj, task.userString)
                val newStatus = updatedDao?.publishStatus()
                (newStatus != oldStatus)
            } else false

        }

        val handleSkillDao = { skillDao: RichSkillDescriptorDao? ->
            skillDao?.let {
                if (publishSkill(it, publishTask)) {
                    modifiedCount += 1
                }
            }
        }

        if (!publishTask.search.uuids.isNullOrEmpty()) {
            totalCount = publishTask.search.uuids.size
            publishTask.search.uuids.forEach { uuid ->
                handleSkillDao(this.findByUUID(uuid))
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
                handleSkillDao(this.findById(hit.content.id))
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

