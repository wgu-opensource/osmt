package edu.wgu.osmt.keyword

import edu.wgu.osmt.api.model.ApiBatchResult
import edu.wgu.osmt.api.model.ApiKeywordUpdate
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.richskill.RichSkillKeywordRepository
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
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

    fun updateFromApi(existingKeywordId: Long, apiKeywordUpdate: ApiKeywordUpdate): KeywordDao?

    fun createFromApi(apiKeywordUpdate: ApiKeywordUpdate): KeywordDao?

    fun remove(existingKeywordId: Long): ApiBatchResult

    fun getDefaultAuthor(): KeywordDao
}

@Repository
@Transactional
class KeywordRepositoryImpl @Autowired constructor(
    val appConfig: AppConfig,
    val keywordEsRepo: KeywordEsRepo,
    val richSkillKeywordRepository: RichSkillKeywordRepository
) : KeywordRepository {

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

    override fun updateFromApi(existingKeywordId: Long, apiKeywordUpdate: ApiKeywordUpdate): KeywordDao? {
        val found = dao.findById(existingKeywordId)
        if (found!=null) {
            transaction {
                found.updateDate = LocalDateTime.now(ZoneOffset.UTC)
                found.value = apiKeywordUpdate.name
                found.uri = apiKeywordUpdate.uri
                found.framework = apiKeywordUpdate.framework
                found.type = apiKeywordUpdate.type
                keywordEsRepo.save(found.toModel())
            }.also { return found }
        } else {
          return null
        }
    }

    override fun createFromApi(apiKeywordUpdate: ApiKeywordUpdate): KeywordDao? {
        var created: KeywordDao? = null
        transaction {
            created = dao.new {
                updateDate = LocalDateTime.now(ZoneOffset.UTC)
                creationDate = LocalDateTime.now(ZoneOffset.UTC)
                this.type = apiKeywordUpdate.type
                this.value = apiKeywordUpdate.name
                this.uri = apiKeywordUpdate.uri
                this.framework = apiKeywordUpdate.framework
            }
                .also { keywordEsRepo.save(it.toModel()) }
        }
        return created
    }

    override fun remove(existingKeywordId: Long): ApiBatchResult {
        val hasRSDsRelated: Boolean
        val keywordFound = dao.findById(existingKeywordId)
        return if (keywordFound != null) {
            hasRSDsRelated = richSkillKeywordRepository.hasRSDsRelated(keywordFound)
            return if(!hasRSDsRelated) {
                transaction {
                    table.deleteWhere { table.id eq existingKeywordId }
                        .also {
                            keywordEsRepo.deleteById(existingKeywordId.toInt())
                        }
                }
                ApiBatchResult(
                    success = true,
                    modifiedCount = 1,
                    totalCount = 1
                )
            } else {
                ApiBatchResult(
                    success = false,
                    modifiedCount = 0,
                    totalCount = 0,
                    message = ErrorMessages.HasRSDRelated.apiValue
                )
            }

        } else {
            ApiBatchResult(
                success = false,
                modifiedCount = 0,
                totalCount = 0,
                message = ErrorMessages.DoesNotExist.apiValue
            )
        }
    }
}

private enum class ErrorMessages(val apiValue: String) {
    DoesNotExist("You cannot delete this item because it does not exist"),
    HasRSDRelated("You cannot delete this item because it is used in one or more RSDs");

    companion object {
        fun forDeleteError(hasRSDsRelated: Boolean): String {
            return if (hasRSDsRelated) {
                HasRSDRelated.apiValue
            } else {
                DoesNotExist.apiValue
            }
        }
    }
}
