package edu.wgu.osmt.keyword

import edu.wgu.osmt.config.AppConfig
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneOffset

interface KeywordRepository {
    val table: KeywordTable
    val dao: KeywordDao.Companion

    fun findAll(): SizedIterable<KeywordDao>
    fun findById(id: Long): KeywordDao?
    fun findByType(type: KeywordTypeEnum): SizedIterable<KeywordDao>
    fun findOrCreate(
        type: KeywordTypeEnum,
        value: String? = null,
        uri: String? = null
    ): KeywordDao

    fun create(
        type: KeywordTypeEnum,
        value: String? = null,
        uri: String? = null
    ): KeywordDao

    fun getDefaultAuthor(): KeywordDao
}


@Repository
class KeywordRepositoryImpl @Autowired constructor(val appConfig: AppConfig) : KeywordRepository {
    override val dao = KeywordDao.Companion
    override val table = KeywordTable

    override fun findAll()  = dao.all()

    override fun findById(id: Long) = dao.findById(id)

    override fun findByType(type: KeywordTypeEnum): SizedIterable<KeywordDao> {
        return dao.wrapRows(table.select { table.keyword_type_enum eq type })
    }

    override fun getDefaultAuthor(): KeywordDao {
        val authorUri:String? = if (appConfig.defaultAuthorUri.isBlank()) null else appConfig.defaultAuthorUri
        return findOrCreate(KeywordTypeEnum.Author, appConfig.defaultAuthorName, authorUri)
    }

    override fun findOrCreate(type: KeywordTypeEnum, value: String?, uri: String?): KeywordDao {
        val existing: KeywordDao? = transaction {
            val condition = when {
                value != null && uri != null ->
                    (table.keyword_type_enum eq type) and (table.value eq value) and (table.uri eq uri)
                value != null ->
                    (table.keyword_type_enum eq type) and (table.value eq value)
                uri != null ->
                    (table.keyword_type_enum eq type) and (table.uri eq uri)
                else -> null
            }

            transaction {
                val query = condition?.let { table.select(it).singleOrNull() }
                query?.let { dao.wrapRow(it) }
            }
        }
        return existing ?: create(type, value, uri)
    }

    override fun create(type: KeywordTypeEnum, value: String?, uri: String?): KeywordDao {
        return transaction {
            dao.new {
                updateDate = LocalDateTime.now(ZoneOffset.UTC)
                creationDate = LocalDateTime.now(ZoneOffset.UTC)
                this.type = type
                this.value = value
                this.uri = uri
            }
        }
    }
}
