package edu.wgu.osmt.keyword

import edu.wgu.osmt.config.AppConfig
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

    fun findAll(): List<Keyword>
    fun findById(id: Long): Keyword?
    fun findByType(type: KeywordTypeEnum): List<Keyword>
    fun findByValueOrUri(
        type: KeywordTypeEnum,
        value: String? = null,
        uri: String? = null
    ): KeywordDao?
    fun findOrCreate(
        type: KeywordTypeEnum,
        value: String? = null,
        uri: String? = null
    ): Keyword

    fun create(
        type: KeywordTypeEnum,
        value: String? = null,
        uri: String? = null
    ): KeywordDao

    fun getDefaultAuthor(): Keyword
}


@Repository
class KeywordRepositoryImpl @Autowired constructor(val appConfig: AppConfig) : KeywordRepository {
    override val dao = KeywordDao.Companion
    override val table = KeywordTable

    override fun findAll() = transaction {
        dao.all().map { it.toModel() }
    }

    override fun findById(id: Long) = transaction {
        dao.findById(id)?.toModel()
    }

    override fun findByType(type: KeywordTypeEnum): List<Keyword> {
        return transaction {
            table.select { table.keyword_type_enum eq type }.map { dao.wrapRow(it).toModel() }
        }
    }

    override fun getDefaultAuthor(): Keyword {
        val authorUri:String? = if (appConfig.defaultAuthorUri.isBlank()) null else appConfig.defaultAuthorUri
        return findOrCreate(KeywordTypeEnum.Author, appConfig.defaultAuthorName, authorUri)
    }

    override fun findOrCreate(type: KeywordTypeEnum, value: String?, uri: String?): Keyword {
        val existing = findByValueOrUri(type, value, uri)
        return existing?.toModel() ?: create(type, value, uri).toModel()
    }

    override fun findByValueOrUri(type: KeywordTypeEnum, value: String?, uri: String?): KeywordDao? {
        return transaction {
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
