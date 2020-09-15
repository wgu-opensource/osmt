package edu.wgu.osmt.richskill

import com.google.gson.Gson
import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.auditlog.AuditOperationType
import edu.wgu.osmt.collection.CollectionSkills
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.db.PublishStatusDao
import edu.wgu.osmt.db.PublishStatusTable
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTable
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
}

@Repository
class RichSkillRepositoryImpl @Autowired constructor(
    val auditLogRepository: AuditLogRepository,
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
            val original = dao.findById(updateObject.id)
            val changes = original?.let { updateObject.diff(it) }

            user?.let { definedUser ->
                changes?.let { it ->
                    auditLogRepository.insert(
                        AuditLog.fromAtomicOp(
                            table,
                            updateObject.id,
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

        return transaction { dao.findById(updateObject.id)?.toModel() }
    }

    override fun findByUUID(uuid: String): RichSkillDescriptor? = transaction {
        val query = table.select { table.uuid eq uuid }.singleOrNull()
        query?.let { dao.wrapRow(it).toModel() }
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
                this.publishStatus =
                    PublishStatusDao[EntityID(PublishStatus.Unpublished.ordinal.toLong(), PublishStatusTable)]
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
}
