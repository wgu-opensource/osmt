package edu.wgu.osmt.richskill

import com.google.gson.Gson
import edu.wgu.osmt.api.FormValidationException
import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.auditlog.AuditOperationType
import edu.wgu.osmt.collection.CollectionSkills
<<<<<<< HEAD
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.jobcode.JobCodeRepository
import edu.wgu.osmt.keyword.*
import org.jetbrains.exposed.dao.id.EntityID
=======
import edu.wgu.osmt.db.updateFromObject
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordRepository
import org.jetbrains.exposed.sql.SizedIterable
>>>>>>> feature/osmt-271-code-cleanup
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
    fun create(
        name: String,
        statement: String,
        author: KeywordDao?,
        user: String,
        category: KeywordDao?
    ): RichSkillDescriptorDao

    fun createFromApi(skillUpdates: List<RsdUpdateDTO>, user: OAuth2User?): List<RichSkillDescriptor>

    fun createFromUpdateObject(updateObject: RsdUpdateObject, user: OAuth2User?): RichSkillDescriptor?
    fun rsdUpdateFromApi(skillUpdate: RsdUpdateDTO): RsdUpdateObject
}

@Repository
@Transactional
class RichSkillRepositoryImpl @Autowired constructor(
    val auditLogRepository: AuditLogRepository,
    val jobCodeRepository: JobCodeRepository,
    val keywordRepository: KeywordRepository,
    val appConfig: AppConfig
) :
    RichSkillRepository {
    override val dao = RichSkillDescriptorDao.Companion
    override val table = RichSkillDescriptorTable

    val richSkillKeywordTable = RichSkillKeywords
    val richSkillJobCodeTable = RichSkillJobCodes
    val richSkillCollectionTable = CollectionSkills

    val keywordDao = KeywordDao.Companion


    override fun findAll() = dao.all()


    override fun findById(id: Long) = dao.findById(id)

    override fun update(updateObject: RsdUpdateObject, user: String): RichSkillDescriptorDao? {

        val original = dao.findById(updateObject.id)
        val changes = original?.let { updateObject.diff(it) }


<<<<<<< HEAD
    @Transactional
    override fun update(updateObject: RsdUpdateObject, user: OAuth2User?): RichSkillDescriptor? {
        transaction {
            val original = dao.findById(updateObject.id!!)
            val changes = original?.let { updateObject.diff(it) }

            user?.let { definedUser ->
                changes?.let { it ->
                    auditLogRepository.insert(
                        AuditLog.fromAtomicOp(
                            table,
                            updateObject.id!!,
                            it.toString(),
                            definedUser,
                            AuditOperationType.Update
                        )
=======
        changes?.let { it ->
            if (it.isNotEmpty())
                auditLogRepository.insert(
                    AuditLog.fromAtomicOp(
                        table,
                        updateObject.id,
                        it.toString(),
                        user,
                        AuditOperationType.Update
>>>>>>> feature/osmt-271-code-cleanup
                    )
                )
        }

        table.updateFromObject(updateObject)

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

<<<<<<< HEAD
        return transaction { dao.findById(updateObject.id!!)?.toModel() }
=======
        return dao.findById(updateObject.id)
>>>>>>> feature/osmt-271-code-cleanup
    }

    override fun findByUUID(uuid: String): RichSkillDescriptorDao? {
        val query = table.select { table.uuid eq uuid }.singleOrNull()
        return query?.let { dao.wrapRow(it) }
    }

<<<<<<< HEAD
    override fun createFromUpdateObject(updateObject: RsdUpdateObject, user: OAuth2User?): RichSkillDescriptor?
    {
        val skill = create(updateObject.name!!, updateObject.statement!!, updateObject.author?.t, user)
        return update(updateObject.copy(id = skill.id.value), user)
    }

=======
    // TODO this might take an "update" object and we can reuse the audit log serializing from that
>>>>>>> feature/osmt-271-code-cleanup
    override fun create(
        name: String,
        statement: String,
        author: KeywordDao?,
        user: String,
        category: KeywordDao?
    ): RichSkillDescriptorDao {
        val authorKeyword: KeywordDao? = author ?: keywordRepository.getDefaultAuthor()

        val newRsd = dao.new {
            this.updateDate = LocalDateTime.now(ZoneOffset.UTC)
            this.creationDate = LocalDateTime.now(ZoneOffset.UTC)
            this.uuid = UUID.randomUUID().toString()
            this.name = name
            this.statement = statement
            this.author = authorKeyword
            this.category = category
        }

        auditLogRepository.insert(
            AuditLog.fromAtomicOp(
                table,
                newRsd.id.value,
                Gson().toJson(newRsd.toModel()),
                user,
                AuditOperationType.Insert
            )
        )

        return newRsd
    }

    override fun createFromApi(skillUpdates: List<RsdUpdateDTO>, user: OAuth2User?): List<RichSkillDescriptor> {
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
            createFromUpdateObject(rsdUpdateObject, user)
        }
        return newSkills.filterNotNull()
    }

    override fun rsdUpdateFromApi(skillUpdate: RsdUpdateDTO): RsdUpdateObject {
        val authorKeyword = skillUpdate.author?.let {
            keywordRepository.findOrCreate(KeywordTypeEnum.Author, value=it.name, uri=it.id)
        }

        val categoryKeyword = skillUpdate.category?.let {
            keywordRepository.findOrCreate(KeywordTypeEnum.Category, value=it)
        }

        val addingKeywords = mutableListOf<Keyword>()
        val removingKeywords = mutableListOf<Keyword>()
        val jobsToAdd = mutableListOf<JobCode>()
        val jobsToRemove = mutableListOf<JobCode>()

        fun lookup_references(lud: ListUpdate, keywordType: KeywordTypeEnum) {
            lud.add?.map {
                keywordRepository.findOrCreate(keywordType, value=it.name, uri=it.id)
            }?.let {
                addingKeywords.addAll(it)
            }

            lud.remove?.map {
                keywordRepository.findByValueOrUri(keywordType, value=it.name, uri=it.id)
            }?.let {
                removingKeywords.addAll(it.map { it?.toModel() }.filterNotNull())
            }
        }

        fun lookup_keywords(slud: StringListUpdate, keywordType: KeywordTypeEnum) {
            slud.add?.map {
                keywordRepository.findOrCreate(keywordType, value=it)
            }?.let {
                addingKeywords.addAll(it)
            }

            slud.remove?.map {
                keywordRepository.findByValueOrUri(keywordType, value=it)
            }?.let {
                removingKeywords.addAll(it.map { it?.toModel() }.filterNotNull())
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
            ListFieldUpdate<Keyword>(
                add=if (addingKeywords.size > 0) addingKeywords else listOf(),
                remove=if (removingKeywords.size > 0) removingKeywords else listOf()
            )
        } else {
            null
        }

        val jobCodesUpdate = if (jobsToAdd.size > 0 || jobsToRemove.size > 0) {
            ListFieldUpdate<JobCode>(
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
            category = categoryKeyword?.let { NullableFieldUpdate(it) },
            keywords = allKeywordsUpdate,
            jobCodes = jobCodesUpdate
        )
    }
}
