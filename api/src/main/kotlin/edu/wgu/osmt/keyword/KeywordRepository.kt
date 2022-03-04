package edu.wgu.osmt.keyword

import edu.wgu.osmt.config.AppConfig
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset

interface KeywordRepository {
    val table: KeywordTable
    val dao: KeywordDao.Companion

    fun findAll(): SizedIterable<KeywordDao>
    fun findById(id: Long): KeywordDao?
    fun findByType(type: KeywordTypeEnum): SizedIterable<KeywordDao>
    fun findByValueOrUri(
        type: KeywordTypeEnum,
        value: String? = null,
        uri: String? = null,
        framework: String? = null
    ): KeywordDao?

    fun findOrCreate(
        type: KeywordTypeEnum,
        value: String? = null,
        uri: String? = null,
        framework: String? = null
    ): KeywordDao?

    fun create(
        type: KeywordTypeEnum,
        value: String? = null,
        uri: String? = null,
        framework: String? = null
    ): KeywordDao?

    fun getDefaultAuthor(): KeywordDao
}


@Repository
@Transactional
class KeywordRepositoryImpl @Autowired constructor(val appConfig: AppConfig) : KeywordRepository {

    @Autowired
    @Lazy
    lateinit var keywordEsRepo: KeywordEsRepo

    override val dao = KeywordDao.Companion
    override val table = KeywordTable

    override fun findAll() = dao.all()

    override fun findById(id: Long) = dao.findById(id)

    override fun findByType(type: KeywordTypeEnum): SizedIterable<KeywordDao> {
        return dao.wrapRows(table.select { table.keyword_type_enum eq type })
    }

    override fun getDefaultAuthor(): KeywordDao {
        val authorUri: String? = if (appConfig.defaultAuthorUri.isBlank()) null else appConfig.defaultAuthorUri
        return findOrCreate(KeywordTypeEnum.Author, appConfig.defaultAuthorName, authorUri)!!
    }

    override fun findOrCreate(type: KeywordTypeEnum, value: String?, uri: String?, framework: String?): KeywordDao? {
        val existing = findByValueOrUri(type, value, uri, framework)
        return existing ?: create(type, value, uri, framework)
    }

    override fun findByValueOrUri(type: KeywordTypeEnum, value: String?, uri: String?, framework: String?): KeywordDao? {
        val condition = (table.keyword_type_enum eq type) and (table.value eq value) and (table.uri eq uri) and (table.framework eq framework)
        val query = table.select(condition).firstOrNull()
        return query?.let { dao.wrapRow(it) }
    }

    override fun create(type: KeywordTypeEnum, value: String?, uri: String?, framework: String?): KeywordDao? {
        val strippedValue = value?.strip()
        val strippedUri = uri?.strip()
        return if (!strippedValue.isNullOrBlank() || !strippedUri.isNullOrBlank()) dao.new {
            updateDate = LocalDateTime.now(ZoneOffset.UTC)
            creationDate = LocalDateTime.now(ZoneOffset.UTC)
            this.type = type
            this.value = value
            this.uri = uri
            this.framework = framework
        }.also { keywordEsRepo.save(it.toModel()) } else null
    }
}
