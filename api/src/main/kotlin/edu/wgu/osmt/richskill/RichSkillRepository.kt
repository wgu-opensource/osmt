package edu.wgu.osmt.richskill

import edu.wgu.osmt.api.FormValidationException
import edu.wgu.osmt.api.model.ApiReferenceListUpdate
import edu.wgu.osmt.api.model.ApiSkillUpdate
import edu.wgu.osmt.api.model.ApiStringListUpdate
import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.auditlog.AuditOperationType
import edu.wgu.osmt.collection.CollectionSkills
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCodeDao
import edu.wgu.osmt.jobcode.JobCodeRepository
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.select
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

interface RichSkillRepository {
    val table: RichSkillDescriptorTable
    val dao: RichSkillDescriptorDao.Companion
    fun update(updateObject: RsdUpdateObject, user: String): RichSkillDescriptorDao?
    fun findAll(): SizedIterable<RichSkillDescriptorDao>
    fun findById(id: Long): RichSkillDescriptorDao?
    fun findByUUID(uuid: String): RichSkillDescriptorDao?
    fun create(updateObject: RsdUpdateObject, user: String): RichSkillDescriptorDao?

    fun createFromApi(skillUpdates: List<ApiSkillUpdate>, user: String): List<RichSkillDescriptorDao>
    fun updateFromApi(existingSkillId: Long, skillUpdate: ApiSkillUpdate, user: String): RichSkillDescriptorDao?
    fun rsdUpdateFromApi(skillUpdate: ApiSkillUpdate): RsdUpdateObject

}

@Repository
@Transactional
class RichSkillRepositoryImpl @Autowired constructor(
    val auditLogRepository: AuditLogRepository,
    val jobCodeRepository: JobCodeRepository,
    val keywordRepository: KeywordRepository
) :
    RichSkillRepository {
    override val dao = RichSkillDescriptorDao.Companion
    override val table = RichSkillDescriptorTable

    val richSkillKeywordTable = RichSkillKeywords
    val richSkillJobCodeTable = RichSkillJobCodes
    val richSkillCollectionTable = CollectionSkills


    override fun findAll() = dao.all()

    override fun findById(id: Long) = dao.findById(id)

    fun applyUpdate(updateObject: RsdUpdateObject): RichSkillDescriptorDao? {
        return dao.findById(updateObject.id!!)?.let { rsdDao ->
            rsdDao.updateDate = LocalDateTime.now(ZoneOffset.UTC)
            when (updateObject.publishStatus) {
                PublishStatus.Archived -> rsdDao.archiveDate = LocalDateTime.now(ZoneOffset.UTC)
                PublishStatus.Published -> rsdDao.publishDate = LocalDateTime.now(ZoneOffset.UTC)
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
                    richSkillKeywordTable.create(richSkillId = updateObject.id, keywordId = keyword.id.value!!)
                }

                it.remove?.forEach { keyword ->
                    richSkillKeywordTable.delete(richSkillId = updateObject.id, keywordId = keyword.id.value!!)
                }
            }

            // update jobcodes
            updateObject.jobCodes?.let {
                it.add?.forEach { jobCode ->
                    richSkillJobCodeTable.create(richSkillId = updateObject.id, jobCodeId = jobCode.id.value!!)
                }
                it.remove?.forEach { jobCode ->
                    richSkillJobCodeTable.delete(richSkillId = updateObject.id, jobCodeId = jobCode.id.value!!)
                }
            }

            // update collections
            updateObject.collections?.let {
                it.add?.forEach { collection ->
                    richSkillCollectionTable.create(
                        collectionId = collection.id.value!!,
                        skillId = updateObject.id
                    )
                }
                it.remove?.forEach { collection ->
                    richSkillCollectionTable.delete(collectionId = collection.id.value!!, skillId = updateObject.id)
                }
            }

            rsdDao
        }
    }

    override fun update(updateObject: RsdUpdateObject, user: String): RichSkillDescriptorDao? {
        val original = dao.findById(updateObject.id!!)
        val changes = original?.let { updateObject.diff(it) }

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

        return applyUpdate(updateObject)
    }

    override fun findByUUID(uuid: String): RichSkillDescriptorDao? {
        val query = table.select { table.uuid eq uuid }.singleOrNull()
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
            id=newRsd.id.value,
            author=updateObject.author ?: NullableFieldUpdate(keywordRepository.getDefaultAuthor())
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
            val rsdUpdateObject = rsdUpdateFromApi(update)
            create(rsdUpdateObject, user)
        }
        return newSkills.filterNotNull()
    }

    override fun updateFromApi(existingSkillId: Long, skillUpdate: ApiSkillUpdate, user: String): RichSkillDescriptorDao? {
        val errors = skillUpdate.validate(0)
        if (errors?.isNotEmpty() == true) {
            throw FormValidationException("Invalid SkillUpdateDescriptor", errors)
        }

        val rsdUpdateObject = rsdUpdateFromApi(skillUpdate)
        val updateObjectWithId = rsdUpdateObject.copy(
            id = existingSkillId
        )
        return update(updateObjectWithId, user)
    }

    override fun rsdUpdateFromApi(skillUpdate: ApiSkillUpdate): RsdUpdateObject {
        val authorKeyword = skillUpdate.author?.let {
            keywordRepository.findOrCreate(KeywordTypeEnum.Author, value=it.name, uri=it.id)
        }

        val categoryKeyword = skillUpdate.category?.let {
            keywordRepository.findOrCreate(KeywordTypeEnum.Category, value=it)
        }

        val addingKeywords = mutableListOf<KeywordDao>()
        val removingKeywords = mutableListOf<KeywordDao>()
        val jobsToAdd = mutableListOf<JobCodeDao>()
        val jobsToRemove = mutableListOf<JobCodeDao>()

        fun lookup_references(lud: ApiReferenceListUpdate, keywordType: KeywordTypeEnum) {
            lud.add?.map {
                keywordRepository.findOrCreate(keywordType, value=it.name, uri=it.id)
            }?.filterNotNull()?.let {
                addingKeywords.addAll(it)
            }

            lud.remove?.map {
                keywordRepository.findByValueOrUri(keywordType, value=it.name, uri=it.id)
            }?.let {
                removingKeywords.addAll(it.filterNotNull())
            }
        }

        fun lookup_keywords(slud: ApiStringListUpdate, keywordType: KeywordTypeEnum) {
            slud.add?.map {
                keywordRepository.findOrCreate(keywordType, value=it)
            }?.filterNotNull()?.let {
                addingKeywords.addAll(it)
            }

            slud.remove?.map {
                keywordRepository.findByValueOrUri(keywordType, value=it)
            }?.let {
                removingKeywords.addAll(it.filterNotNull())
            }
        }

        skillUpdate.occupations?.let {
            it.add?.map {
                jobCodeRepository.findByCodeOrCreate(code=it)
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
                add=if (addingKeywords.size > 0) addingKeywords else listOf(),
                remove=if (removingKeywords.size > 0) removingKeywords else listOf()
            )
        } else {
            null
        }

        val jobCodesUpdate = if (jobsToAdd.size > 0 || jobsToRemove.size > 0) {
            ListFieldUpdate<JobCodeDao>(
                add=if (jobsToAdd.size > 0) jobsToAdd else listOf(),
                remove=if (jobsToRemove.size > 0) jobsToRemove else listOf()
            )
        } else {
            null
        }

        return RsdUpdateObject(
            name = skillUpdate.skillName,
            statement = skillUpdate.skillStatement,
            publishStatus = skillUpdate.publishStatus,
            author = authorKeyword?.let { NullableFieldUpdate(it) },
            category = if(skillUpdate.category != null || skillUpdate.category?.isBlank() == true) NullableFieldUpdate(categoryKeyword) else null,
            keywords = allKeywordsUpdate,
            jobCodes = jobCodesUpdate
        )
    }

}
