package edu.wgu.osmt.collection

import edu.wgu.osmt.api.FormValidationException
import edu.wgu.osmt.api.model.ApiBatchResult
import edu.wgu.osmt.api.model.ApiCollectionUpdate
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.auditlog.AuditOperationType
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.elasticsearch.EsCollectionRepository
import edu.wgu.osmt.elasticsearch.EsRichSkillRepository
import edu.wgu.osmt.elasticsearch.SearchService
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.richskill.RichSkillDoc
import edu.wgu.osmt.task.UpdateCollectionSkillsTask
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.select
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
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

    fun updateSkillsForTask(
        collectionUuid: String,
        task: UpdateCollectionSkillsTask,
        richSkillRepository: RichSkillRepository
    ): ApiBatchResult
}


@Repository
@Transactional
class CollectionRepositoryImpl @Autowired constructor(
    val keywordRepository: KeywordRepository,
    val auditLogRepository: AuditLogRepository,
    val esRichSkillRepository: EsRichSkillRepository,
    val esCollectionRepository: EsCollectionRepository,
    val searchService: SearchService,
    val appConfig: AppConfig
) : CollectionRepository {
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
        return create(CollectionUpdateObject(name=name), user)
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
        }

        val updateWithIdAndAuthor = updateObject.copy(
            id = newCollection.id.value
        )
        return update(updateWithIdAndAuthor, user)
    }

    fun applyUpdate(collectionDao: CollectionDao, updateObject: CollectionUpdateObject): Unit {
        collectionDao.updateDate = LocalDateTime.now(ZoneOffset.UTC)

        when (updateObject.publishStatus) {
            PublishStatus.Archived -> collectionDao.archiveDate = LocalDateTime.now(ZoneOffset.UTC)
            PublishStatus.Published -> collectionDao.publishDate = LocalDateTime.now(ZoneOffset.UTC)
            PublishStatus.Unpublished -> {
            } // non-op
        }

        updateObject.name?.let { collectionDao.name = it }

        updateObject.author?.let {
            if (it.t != null) {
                collectionDao.author = it.t
            } else {
                collectionDao.author = null
            }
        }

        updateObject.skills?.let {
            it.add?.forEach { skill ->
                CollectionSkills.create(collectionId = updateObject.id!!, skillId = skill.id.value)
            }
            it.remove?.forEach { skill ->
                CollectionSkills.delete(collectionId = updateObject.id!!, skillId = skill.id.value)
            }
        }
    }

    override fun update(updateObject: CollectionUpdateObject, user: String): CollectionDao? {
        val daoObject = dao.findById(updateObject.id!!)
        val changes = daoObject?.let { updateObject.diff(it) }

        daoObject?.let {
            applyUpdate(it, updateObject)

            // reindex elastic search documents
            esCollectionRepository.save(it.toDoc())
            esRichSkillRepository.saveAll(it.skills.map { skill -> RichSkillDoc.fromDao(skill, appConfig) })
        }

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
        return daoObject
    }

    override fun createFromApi(apiUpdates: List<ApiCollectionUpdate>, richSkillRepository: RichSkillRepository, user: String): List<CollectionDao> {
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

    override fun updateSkillsForTask(
        collectionUuid: String,
        task: UpdateCollectionSkillsTask,
        richSkillRepository: RichSkillRepository
    ): ApiBatchResult {
        var modifiedCount = 0
        var totalCount = 0

        val collectionId = this.findByUUID(collectionUuid)!!.id.value

        task.skillListUpdate.remove?.forEach{ skillUuid ->
            val skillId = richSkillRepository.findByUUID(skillUuid)?.id?.value
            skillId?.let {
                modifiedCount += CollectionSkills.delete(collectionId, it)
            }
            totalCount += 1
        }

        val add_skill_dao = {skillDao: RichSkillDescriptorDao? ->
            skillDao?.let {
                CollectionSkills.create(collectionId, skillDao.id.value)
                modifiedCount += 1
            }
        }

        if (!task.skillListUpdate.add?.uuids.isNullOrEmpty()) {
            task.skillListUpdate.add?.uuids?.forEach { uuid ->
                val skillDao = richSkillRepository.findByUUID(uuid)
                add_skill_dao(skillDao)
            }
            totalCount += task.skillListUpdate.add?.uuids?.size ?: 0
        } else if (task.skillListUpdate.add != null) {
            val searchHits = searchService.searchRichSkillsByApiSearch(
                task.skillListUpdate.add,
                task.publishStatuses,
                Pageable.unpaged()
            )
            searchHits.forEach { hit ->
                add_skill_dao(richSkillRepository.findById(hit.content.id))
            }
            totalCount += searchHits.totalHits.toInt()
        }

        return ApiBatchResult(
            success = true,
            modifiedCount = modifiedCount,
            totalCount = totalCount
        )
    }

    override fun collectionUpdateObjectFromApi(collectionUpdate: ApiCollectionUpdate, richSkillRepository: RichSkillRepository): CollectionUpdateObject {
        val authorKeyword = collectionUpdate.author?.let {
            keywordRepository.findOrCreate(KeywordTypeEnum.Author, value = it.name, uri = it.id)
        }

        val adding = mutableListOf<RichSkillDescriptorDao>()
        val removing = mutableListOf<RichSkillDescriptorDao>()
        collectionUpdate.skills?.let {slu ->
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
}
