package edu.wgu.osmt.collection

import edu.wgu.osmt.api.FormValidationException
import edu.wgu.osmt.api.GeneralApiException
import edu.wgu.osmt.api.model.ApiBatchResult
import edu.wgu.osmt.api.model.ApiCollection
import edu.wgu.osmt.api.model.ApiCollectionUpdate
import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.auditlog.AuditOperationType
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.auditlog.Change
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.*
import edu.wgu.osmt.task.PublishTask
import edu.wgu.osmt.task.ShareExternallyTask
import edu.wgu.osmt.task.TaskMessageService
import edu.wgu.osmt.task.UpdateCollectionSkillsTask
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.select
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

interface CollectionRepository {
    val table: CollectionTable
    val dao: CollectionDao.Companion

    fun findAll(): SizedIterable<CollectionDao>
    fun findById(id: Long): CollectionDao?
    fun findByUUID(uuid: String): CollectionDao?
    fun findByName(name: String): CollectionDao?
    fun create(name: String, user: String): CollectionDao?
    fun create(updateObject: CollectionUpdateObject, user: String): CollectionDao?
    fun update(updateObject: CollectionUpdateObject, user: String): CollectionDao?

    fun importFromApi(
        apiCollection: ApiCollection,
        originalUrl: String,
        originalLibraryName: String?,
        richSkillRepository: RichSkillRepository,
        user: String
    ): CollectionDao?

    fun createFromApi(
        apiUpdates: List<ApiCollectionUpdate>,
        richSkillRepository: RichSkillRepository,
        user: String
    ): List<CollectionDao>

    fun collectionUpdateObjectFromApi(
        collectionUpdate: ApiCollectionUpdate,
        richSkillRepository: RichSkillRepository
    ): CollectionUpdateObject

    fun updateFromApi(
        existingCollectionId: Long,
        collectionUpdate: ApiCollectionUpdate,
        richSkillRepository: RichSkillRepository,
        user: String
    ): CollectionDao?

    fun updateIsExternallyShared(
        id: Long,
        newValue: Boolean,
        user: String
    ): CollectionDao?

    fun updateSkillsForTask(
        collectionUuid: String,
        task: UpdateCollectionSkillsTask,
        richSkillRepository: RichSkillRepository
    ): ApiBatchResult

    fun changeStatusesForTask(publishTask: PublishTask): ApiBatchResult?
}


@Repository
@Transactional
class CollectionRepositoryImpl @Autowired constructor(
    val auditLogRepository: AuditLogRepository,
    val richSkillEsRepo: RichSkillEsRepo,
    val collectionEsRepo: CollectionEsRepo,
    val taskMessageService: TaskMessageService,
    val appConfig: AppConfig
) : CollectionRepository {

    @Autowired
    @Lazy
    lateinit var keywordRepository: KeywordRepository

    override val table = CollectionTable
    override val dao = CollectionDao.Companion

    override fun findAll() = dao.all()

    override fun findById(id: Long) = dao.findById(id)

    override fun findByUUID(uuid: String): CollectionDao? {
        val query = table.select { table.uuid eq uuid }.firstOrNull()
        return query?.let { dao.wrapRow(it) }
    }

    override fun findByName(name: String): CollectionDao? {
        val query = table.select { table.name eq name }.firstOrNull()
        return query?.let { dao.wrapRow(it) }
    }

    override fun create(name: String, user: String): CollectionDao? {
        return create(CollectionUpdateObject(name = name), user)
    }

    override fun importFromApi(
            apiCollection: ApiCollection,
            originalUrl: String,
            originalLibraryName: String?,
            richSkillRepository: RichSkillRepository,
            user: String
    ): CollectionDao? {
        val apiCollectionUpdate: ApiCollectionUpdate = ApiCollectionUpdate.fromApiCollection(apiCollection)
        val collectionUpdateObject = collectionUpdateObjectFromApi(apiCollectionUpdate, richSkillRepository)

        val existingDao = table.select { table.importedFrom eq originalUrl }.firstOrNull()?.let { dao.wrapRow(it) }
        val collectionDao = if (existingDao != null) {
            update(collectionUpdateObject.copy(id=existingDao.id.value), user)
        } else {
            create(collectionUpdateObject, user)
        }

        if (collectionDao != null) {
            collectionDao.publishDate = apiCollection.publishDate?.toLocalDateTime()
            collectionDao.archiveDate = apiCollection.archiveDate?.toLocalDateTime()
            collectionDao.importedFrom = originalUrl
            collectionDao.libraryName = originalLibraryName
            collectionEsRepo.save(collectionDao.toDoc())
        }
        return collectionDao
    }

    override fun create(updateObject: CollectionUpdateObject, user: String): CollectionDao? {
        if (updateObject.name.isNullOrBlank()) {
            return null
        }

        val newCollection = dao.new {
            this.creationDate = LocalDateTime.now(ZoneOffset.UTC)
            this.updateDate = this.creationDate
            this.uuid = UUID.randomUUID().toString()
            this.name = updateObject.name
            this.author = updateObject.author?.t
            this.isExternallyShared = false
        }

        updateObject.copy(id = newCollection.id.value).applyToDao(newCollection)

        newCollection.let {
            collectionEsRepo.save(it.toDoc())
            richSkillEsRepo.saveAll(it.skills.map { skill -> RichSkillDoc.fromDao(skill, appConfig) })
        }

        auditLogRepository.create(
            AuditLog.fromAtomicOp(
                table,
                newCollection.id.value,
                newCollection.toModel().diff(null),
                user,
                AuditOperationType.Insert
            )
        )

        return newCollection
    }

    override fun update(updateObject: CollectionUpdateObject, user: String): CollectionDao? {
        val daoObject = dao.findById(updateObject.id!!)
        val oldObject = daoObject?.toModel()
        val currentSkillDaos = daoObject?.skills?.toList().orEmpty()

        val skillDaosToTrack = (updateObject.skills?.add.orEmpty() + updateObject.skills?.remove.orEmpty() + currentSkillDaos).distinct()
        val oldSkills = skillDaosToTrack.map { it.uuid to it.toModel() }.toMap()

        daoObject?.let {
            updateObject.applyToDao(it)

            // reindex elastic search documents
            collectionEsRepo.save(it.toDoc())
            richSkillEsRepo.saveAll(it.skills.map { skill -> RichSkillDoc.fromDao(skill, appConfig) })
        }

        val (publishStatusChanges, otherChanges) = daoObject?.toModel()?.diff(oldObject)
            ?.partition { it.fieldName == Collection::publishStatus.name } ?: (null to null)

        // catch collection/skill relationship changes and generate audit log(s)
        skillDaosToTrack.map { skillDao ->
            val oldSkill = oldSkills.get(skillDao.uuid)
            val skillChange = skillDao.toModel().diff(oldSkill)
            if (skillChange.isNotEmpty()) {
                auditLogRepository.create(
                    AuditLog.fromAtomicOp(
                        RichSkillDescriptorTable,
                        skillDao.id.value,
                        skillChange,
                        user,
                        AuditOperationType.Update
                    )
                )
            }
        }

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

        return daoObject
    }

    override fun createFromApi(
        apiUpdates: List<ApiCollectionUpdate>,
        richSkillRepository: RichSkillRepository,
        user: String
    ): List<CollectionDao> {
        // pre validate all rows
        val allErrors = apiUpdates.mapIndexed { i, updateDto ->
            updateDto.validateForCreation(i)
        }.filterNotNull().flatten()
        if (allErrors.isNotEmpty()) {
            throw FormValidationException("Invalid SkillUpdateDescriptor", allErrors)
        }

        // create records
        val newSkills = apiUpdates.map { update ->
            val updateObject = collectionUpdateObjectFromApi(update, richSkillRepository)
            create(updateObject, user)
        }
        return newSkills.filterNotNull()
    }

    override fun updateFromApi(
        existingCollectionId: Long,
        collectionUpdate: ApiCollectionUpdate,
        richSkillRepository: RichSkillRepository,
        user: String
    ): CollectionDao? {
        val errors = collectionUpdate.validate(0)
        if (errors?.isNotEmpty() == true) {
            throw FormValidationException("Invalid SkillUpdateDescriptor", errors)
        }

        val collectionUpdateObject = collectionUpdateObjectFromApi(collectionUpdate, richSkillRepository)

        val updateObjectWithId = collectionUpdateObject.copy(
            id = existingCollectionId
        )

        return update(updateObjectWithId, user)
    }

    override fun updateIsExternallyShared(id: Long, newValue: Boolean, user: String): CollectionDao? {
        val daoObject = dao.findById(id)

        daoObject?.let {
            if (daoObject.isExternallyShared != newValue) {
                if (newValue && !PublishStatus.isCurrentlyPublished(daoObject.publishStatus())) {
                    throw GeneralApiException("Collection must be published to share externally", HttpStatus.FORBIDDEN)
                }

                daoObject.isExternallyShared = newValue;

                if (appConfig.searchHubConfigured) {
                    val task = ShareExternallyTask(
                            canonicalUrl = daoObject.canonicalUrl(appConfig.baseUrl),
                            shared = newValue
                    )
                    taskMessageService.enqueueJob(TaskMessageService.shareCollectionExternally, task)
                }

                auditLogRepository.create(
                    AuditLog.fromAtomicOp(
                        table,
                        id,
                        listOf(
                            Change(
                                Collection::isExternallyShared.name,
                                (!newValue).toString(),
                                newValue.toString()
                            )
                        ),
                        user,
                        AuditOperationType.ExternalSharingChange
                    )
                )
            }
        }

        return daoObject
    }

    override fun updateSkillsForTask(
        collectionUuid: String,
        task: UpdateCollectionSkillsTask,
        richSkillRepository: RichSkillRepository
    ): ApiBatchResult {
        var modifiedCount = 0
        var totalCount = 0

        val collectionDao = this.findByUUID(collectionUuid)
        val collectionId = collectionDao!!.id.value

        val modifiedSkillDaos = mutableListOf<RichSkillDescriptorDao>()
        val modifiedSkillOriginals = mutableMapOf<String, RichSkillDescriptor>()

        //process additions
        fun addSkillDao(skillDao: RichSkillDescriptorDao?) {
            skillDao?.let { dao ->
                modifiedSkillDaos.add(dao).also { modifiedSkillOriginals.put(dao.uuid, dao.toModel()) }
                CollectionSkills.create(collectionId, dao.id.value)
                richSkillEsRepo.save(RichSkillDoc.fromDao(dao, appConfig))
                modifiedCount += 1
            }
        }

        fun removeSkillDao(skillDao: RichSkillDescriptorDao?) {
            skillDao?.let { dao ->
                modifiedSkillDaos.add(dao).also { modifiedSkillOriginals.put(dao.uuid, dao.toModel()) }
                CollectionSkills.delete(collectionId, dao.id.value)
                richSkillEsRepo.save(RichSkillDoc.fromDao(dao, appConfig))
                modifiedCount += 1
            }
        }

        if (!task.skillListUpdate.add?.uuids.isNullOrEmpty()) {
            task.skillListUpdate.add?.uuids?.forEach { uuid ->
                val skillDao = richSkillRepository.findByUUID(uuid)
                addSkillDao(skillDao)
            }
            totalCount += task.skillListUpdate.add?.uuids?.size ?: 0
        } else if (task.skillListUpdate.add != null) {
            val searchHits = richSkillEsRepo.byApiSearch(
                task.skillListUpdate.add,
                task.publishStatuses,
                Pageable.unpaged()
            )
            searchHits.forEach { hit ->
                addSkillDao(richSkillRepository.findById(hit.content.id))
            }
            totalCount += searchHits.totalHits.toInt()
        }

        // process removals
        if (!task.skillListUpdate.remove?.uuids.isNullOrEmpty()) {
            task.skillListUpdate.remove?.uuids?.forEach { skillUuid ->
                val skillDao = richSkillRepository.findByUUID(skillUuid)
                removeSkillDao(skillDao)
                totalCount += 1
            }
        } else if (task.skillListUpdate.remove != null) {
            val searchHits = richSkillEsRepo.byApiSearch(
                task.skillListUpdate.remove,
                task.publishStatuses,
                Pageable.unpaged()
            )
            searchHits.forEach { hit ->
                removeSkillDao(richSkillRepository.findById(hit.content.id))
            }
            totalCount += searchHits.totalHits.toInt()

        }

        modifiedSkillDaos.map{skillDao ->
            val changes = skillDao.toModel().diff(modifiedSkillOriginals[skillDao.uuid])
            if (changes.isNotEmpty()) {
                auditLogRepository.create(
                    AuditLog.fromAtomicOp(
                        RichSkillDescriptorTable,
                        skillDao.id.value,
                        changes,
                        task.userString,
                        AuditOperationType.Update
                    )
                )
            }
        }

        // update affected elasticsearch indexes
        this.findByUUID(collectionUuid)?.let {
            collectionEsRepo.save(it.toDoc())
        }

        return ApiBatchResult(
            success = true,
            modifiedCount = modifiedCount,
            totalCount = totalCount
        )
    }

    override fun collectionUpdateObjectFromApi(
        collectionUpdate: ApiCollectionUpdate,
        richSkillRepository: RichSkillRepository
    ): CollectionUpdateObject {
        val authorKeyword = collectionUpdate.author?.let {
            keywordRepository.findOrCreate(KeywordTypeEnum.Author, value = it)
        }

        val adding = mutableListOf<RichSkillDescriptorDao>()
        val removing = mutableListOf<RichSkillDescriptorDao>()
        collectionUpdate.skills?.let { slu ->
            slu.add?.mapNotNull {
                richSkillRepository.findByUUID(it)
            }?.let {
                adding.addAll(it)
            }

            slu.remove?.mapNotNull {
                richSkillRepository.findByUUID(it)
            }?.let {
                removing.addAll(it)
            }
        }

        return CollectionUpdateObject(
            name = collectionUpdate.name,
            publishStatus = collectionUpdate.publishStatus,
            author = authorKeyword?.let { NullableFieldUpdate(it) },
            skills = if (adding.size + removing.size > 0) ListFieldUpdate(adding, removing) else null
        )
    }

    override fun changeStatusesForTask(publishTask: PublishTask): ApiBatchResult? {
        var modifiedCount = 0
        var totalCount = 0

        fun publishCollection(collectionDao: CollectionDao, task: PublishTask): Boolean {
            val oldStatus = collectionDao.publishStatus()
            return if (oldStatus != task.publishStatus) {
                val updateObj = CollectionUpdateObject(id = collectionDao.id.value, publishStatus = task.publishStatus)
                val updatedDao = this.update(updateObj, task.userString)
                val newStatus = updatedDao?.publishStatus()
                (newStatus != oldStatus)
            } else false
        }

        fun handleCollectionDao(collectionDao: CollectionDao?): Unit {
            collectionDao?.let {
                // imported skills are readonly
                if (it.importedFrom == null) {
                    if (publishCollection(it, publishTask)) {
                        modifiedCount += 1
                    }
                }
            }
        }

        if (!publishTask.search.uuids.isNullOrEmpty()) {
            totalCount = publishTask.search.uuids.size
            publishTask.search.uuids.forEach { uuid ->
                handleCollectionDao(this.findByUUID(uuid))
            }
        } else {
            val searchHits = collectionEsRepo.byApiSearch(
                publishTask.search,
                publishTask.filterByStatus,
                Pageable.unpaged()
            )
            totalCount = searchHits.totalHits.toInt()
            searchHits.forEach { hit ->
                handleCollectionDao(this.findById(hit.content.id))
            }
        }

        return ApiBatchResult(
            success = true,
            modifiedCount = modifiedCount,
            totalCount = totalCount
        )
    }
}
