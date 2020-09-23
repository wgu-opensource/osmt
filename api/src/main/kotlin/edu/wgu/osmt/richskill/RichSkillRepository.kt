package edu.wgu.osmt.richskill

import com.google.gson.Gson
import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.auditlog.AuditOperationType
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.collection.CollectionSkills
import edu.wgu.osmt.db.updateFromObject
import edu.wgu.osmt.jobcode.JobCodeDao
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordRepository
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
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
        category: KeywordDao?,
        keywords: List<KeywordDao>? = null,
        collections: List<CollectionDao>? = null,
        jobCodes: List<JobCodeDao>? = null
    ): RichSkillDescriptorDao
}

@Repository
@Transactional
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


    override fun findAll() = dao.all()


    override fun findById(id: Long) = dao.findById(id)

    override fun update(updateObject: RsdUpdateObject, user: String): RichSkillDescriptorDao? {

        val original = dao.findById(updateObject.id)
        val changes = original?.let { updateObject.diff(it) }


        changes?.let { it ->
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
            it.remove?.forEach { collection ->
                richSkillCollectionTable.delete(collectionId = collection.id!!, skillId = updateObject.id)
            }
        }

        return dao.findById(updateObject.id)
    }

    override fun findByUUID(uuid: String): RichSkillDescriptorDao? {
        val query = table.select { table.uuid eq uuid }.singleOrNull()
        return query?.let { dao.wrapRow(it) }
    }

    override fun create(
        name: String,
        statement: String,
        author: KeywordDao?,
        user: String,
        category: KeywordDao?,
        keywords: List<KeywordDao>?,
        collections: List<CollectionDao>?,
        jobCodes: List<JobCodeDao>?
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

        // create the relationships between entities
        keywords?.let {
            newRsd.keywords = SizedCollection(it)
        }
        collections?.let {
            newRsd.collections = SizedCollection(it)
        }
        jobCodes?.let {
            newRsd.jobCodes = SizedCollection(it)
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
}
