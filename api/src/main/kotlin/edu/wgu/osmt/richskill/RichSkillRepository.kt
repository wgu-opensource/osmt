package edu.wgu.osmt.richskill

import com.google.gson.Gson
import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.auditlog.AuditOperationType
import edu.wgu.osmt.collection.CollectionSkills
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.jobcode.JobCodeRepository
import edu.wgu.osmt.keyword.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

interface RichSkillRepository {
    val table: RichSkillDescriptorTable
    val dao: RichSkillDescriptorDao.Companion
    fun update(updateObject: RsdUpdateObject, user: OAuth2User?): RichSkillDescriptor?
    fun findAll(includeCollections: Boolean = false): List<RichSkillDescriptor>
    fun findById(id: Long): RichSkillDescriptor?
    fun findByUUID(uuid: String): RichSkillDescriptor?
    fun create(
        name: String,
        statement: String,
        author: Keyword?,
        user: OAuth2User?
    ): RichSkillDescriptorDao
    fun createFromUpdateObject(updateObject: RsdUpdateObject, user: OAuth2User?): RichSkillDescriptor?
    fun rsdUpdateFromApi(skillUpdate: RsdUpdateDTO): RsdUpdateObject
}

@Repository
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

    val keywordDao = KeywordDao.Companion


    override fun findAll(includeCollections: Boolean) = transaction {
        dao.all().map { r ->
            val model = r.toModel()
            if (includeCollections) {
                model.collections = r.collections.map { it.toModel() }
            }
            model
        }
    }

    override fun findById(id: Long) = transaction {
        dao.findById(id)?.toModel()
    }

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
                    )
                }
            }

            table.update({ table.id eq updateObject.id }) {
                updateBuilderApplyFromUpdateObject(it, updateObject)
            }

            // update keywords
            updateObject.keywords?.let {

                it.add?.forEach { keyword ->
                    richSkillKeywordTable.create(richSkillId = updateObject.id, keywordId = keyword.id!!)
                }

                it.remove?.forEach { keyword ->
                    richSkillKeywordTable.delete(richSkillId = updateObject.id, keywordId = keyword.id!!)
                }
            }

            // update jobcodes
            updateObject.jobCodes?.let {
                it.add?.forEach { jobCode ->
                    richSkillJobCodeTable.create(richSkillId = updateObject.id, jobCodeId = jobCode.id!!)
                }
                it.remove?.forEach { jobCode ->
                    richSkillJobCodeTable.delete(richSkillId = updateObject.id, jobCodeId = jobCode.id!!)
                }
            }

            // update collections
            updateObject.collections?.let {
                it.add?.forEach { collection ->
                    richSkillCollectionTable.create(
                        collectionId = collection.id!!,
                        skillId = updateObject.id
                    )
                }
                it.remove?.forEach {collection ->
                    richSkillCollectionTable.delete(collectionId = collection.id!!, skillId = updateObject.id)
                }
            }
        }

        return transaction { dao.findById(updateObject.id!!)?.toModel() }
    }

    override fun findByUUID(uuid: String): RichSkillDescriptor? = transaction {
        val query = table.select { table.uuid eq uuid }.singleOrNull()
        query?.let { dao.wrapRow(it).toModel() }
    }

    override fun createFromUpdateObject(updateObject: RsdUpdateObject, user: OAuth2User?): RichSkillDescriptor?
    {
        val skill = create(updateObject.name!!, updateObject.statement!!, updateObject.author?.t, user)
        return update(updateObject.copy(id = skill.id.value), user)
    }

    override fun create(
        name: String,
        statement: String,
        author: Keyword?,
        user: OAuth2User?
    ): RichSkillDescriptorDao {
        val newRichSkill = transaction {
            val authorKeyword = author ?: keywordRepository.getDefaultAuthor()
            dao.new {
                this.updateDate = LocalDateTime.now(ZoneOffset.UTC)
                this.creationDate = LocalDateTime.now(ZoneOffset.UTC)
                this.uuid = UUID.randomUUID().toString()
                this.name = name
                this.statement = statement
                this.author = KeywordDao[EntityID(authorKeyword.id!!, KeywordTable)]
            }
        }
        user?.let { definedUser ->
            transaction {
                auditLogRepository.insert(
                    AuditLog.fromAtomicOp(
                        table,
                        newRichSkill.id.value,
                        Gson().toJson(newRichSkill.toModel()),
                        definedUser,
                        AuditOperationType.Insert
                    )
                )
            }
        }
        return newRichSkill
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
