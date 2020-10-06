package edu.wgu.osmt.collection

import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.db.updateFromObject
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import edu.wgu.osmt.richskill.RsdUpdateObject
import org.jetbrains.exposed.sql.SizedIterable
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

interface CollectionRepository {
    val table: CollectionTable
    val dao: CollectionDao.Companion

    fun findAll(): SizedIterable<CollectionDao>
    fun findById(id: Long): CollectionDao?
    fun findByUUID(uuid: String): CollectionDao?
    fun findByName(name: String): CollectionDao?
    fun create(name: String, author: KeywordDao? = null): CollectionDao
    fun update(updateObject: CollectionUpdateObject, user: OAuth2User? = null): CollectionDao?
}


@Repository
@Transactional
class CollectionRepositoryImpl @Autowired constructor(val keywordRepository: KeywordRepository): CollectionRepository {
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
       }
    }

    fun applyUpdate(updateObject: CollectionUpdateObject): CollectionDao? {
        return dao.findById(updateObject.id!!)?.let { collectionDao ->
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

            collectionDao
        }
    }

    override fun update(updateObject: CollectionUpdateObject, user: OAuth2User?): CollectionDao? {
        return applyUpdate(updateObject)
    }
}
