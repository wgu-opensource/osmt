package edu.wgu.osmt.collection

import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTable
import edu.wgu.osmt.richskill.RichSkillDescriptor
import edu.wgu.osmt.richskill.RsdUpdateObject
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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

    fun findAll(): List<Collection>
    fun findById(id: Long): Collection?
    fun findByUUID(uuid: String): Collection?
    fun findByName(name: String): Collection?
    fun create(name: String, author: Keyword? = null): CollectionDao
    fun update(updateObject: CollectionUpdateObject, user: OAuth2User? = null): Collection?
}


@Repository
class CollectionRepositoryImpl @Autowired constructor(val keywordRepository: KeywordRepository): CollectionRepository {
    override val table = CollectionTable
    override val dao = CollectionDao.Companion

    override fun findAll() = transaction {
        dao.all().map { it.toModel() }
    }

    override fun findById(id: Long) = transaction {
        dao.findById(id)?.toModel()
    }

    override fun findByUUID(uuid: String): Collection? = transaction {
        val query = table.select { table.uuid eq uuid }.singleOrNull()
        query?.let { dao.wrapRow(it).toModel() }
    }

   override fun findByName(name: String): Collection? = transaction {
        val query = table.select { table.name eq name }.singleOrNull()
        query?.let { dao.wrapRow(it).toModel() }
    }

    override fun create(name: String, author: Keyword?): CollectionDao {
       return transaction {
           val authorKeyword = author ?: keywordRepository.getDefaultAuthor()
           dao.new {
               this.updateDate = LocalDateTime.now(ZoneOffset.UTC)
               this.creationDate = LocalDateTime.now(ZoneOffset.UTC)
               this.updateDate = this.creationDate
               this.uuid = UUID.randomUUID().toString()
               this.name = name
               this.author = KeywordDao[EntityID(authorKeyword.id!!, KeywordTable)]
           }
       }
    }

    @Transactional
    override fun update(updateObject: CollectionUpdateObject, user: OAuth2User?): Collection? {
        transaction {
            val original = dao.findById(updateObject.id)
            val changes = original?.let { updateObject.diff(it) }
            table.update({table.id eq updateObject.id}) {
                updateBuilderApplyFromUpdateObject(it, updateObject)
            }

            // update skills
            updateObject.skills?.let {
                it.add?.forEach { skill ->
                    CollectionSkills.create(collectionId = updateObject.id, skillId = skill.id!!)
                }
                it.remove?.forEach { skill ->
                    CollectionSkills.delete(collectionId = updateObject.id, skillId = skill.id!!)
                }
            }
        }
        return transaction { dao.findById(updateObject.id)?.toModel() }
    }
}
