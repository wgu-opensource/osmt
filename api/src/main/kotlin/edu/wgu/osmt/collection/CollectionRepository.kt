package edu.wgu.osmt.collection

import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.auditlog.AuditOperationType
import edu.wgu.osmt.elasticsearch.EsCollectionRepository
import edu.wgu.osmt.elasticsearch.EsRichSkillRepository
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordRepository
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.select
import org.springframework.beans.factory.annotation.Autowired
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
    fun create(name: String, author: KeywordDao? = null): CollectionDao
    fun update(updateObject: CollectionUpdateObject, user: String): CollectionDao?
}


@Repository
@Transactional
class CollectionRepositoryImpl @Autowired constructor(
    val keywordRepository: KeywordRepository,
    val auditLogRepository: AuditLogRepository,
    val esRichSkillRepository: EsRichSkillRepository,
    val esCollectionRepository: EsCollectionRepository
) : CollectionRepository {
    override val table = CollectionTable
    override val dao = CollectionDao.Companion

    override fun findAll() = dao.all()

    override fun findById(id: Long) = dao.findById(id)

    override fun findByUUID(uuid: String): CollectionDao? {
        val query = table.select { table.uuid eq uuid }.singleOrNull()
        return query?.let { dao.wrapRow(it) }
    }

    override fun findByName(name: String): CollectionDao? {
        val query = table.select { table.name eq name }.singleOrNull()
        return query?.let { dao.wrapRow(it) }
    }

    override fun create(name: String, author: KeywordDao?): CollectionDao {
        val authorKeyword: KeywordDao? = author ?: keywordRepository.getDefaultAuthor()
        return dao.new {
            this.updateDate = LocalDateTime.now(ZoneOffset.UTC)
            this.creationDate = LocalDateTime.now(ZoneOffset.UTC)
            this.updateDate = this.creationDate
            this.uuid = UUID.randomUUID().toString()
            this.name = name
            this.author = authorKeyword
        }.also { esCollectionRepository.save(it.toDoc()) }
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
                CollectionSkills.create(collectionId = updateObject.id, skillId = skill.id.value)
            }
            it.remove?.forEach { skill ->
                CollectionSkills.delete(collectionId = updateObject.id, skillId = skill.id.value)
            }
        }
    }

    override fun update(updateObject: CollectionUpdateObject, user: String): CollectionDao? {
        val daoObject = dao.findById(updateObject.id)
        val changes = daoObject?.let { updateObject.diff(it) }

        daoObject?.let {
            applyUpdate(it, updateObject)

            // reindex elastic search documents
            esCollectionRepository.save(it.toDoc())
            esRichSkillRepository.saveAll(it.skills.map { skill -> skill.toDoc() })
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
}
