package edu.wgu.osmt.auditlog

import edu.wgu.osmt.api.model.SkillSortEnum
import edu.wgu.osmt.api.model.SortOrder as SortOrder
import edu.wgu.osmt.api.model.SortOrderCompanion
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.sortAdapter
import edu.wgu.osmt.elasticsearch.OffsetPageable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder as ExposedSortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.util.Streamable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

interface AuditLogRepository {
    val table: AuditLogTable
    val dao: AuditLogDao.Companion

    fun create(auditLog: AuditLog): AuditLogDao?
    fun findByTableAndId(tableName: String, entityId: Long, offsetPageable: OffsetPageable? = null): SizedIterable<AuditLogDao>
}

@Repository
@Transactional
class AuditLogRepositoryImpl @Autowired constructor(appConfig: AppConfig) : AuditLogRepository {
    override val table = AuditLogTable
    override val dao = AuditLogDao.Companion

    override fun create(auditLog: AuditLog): AuditLogDao? {
        if (auditLog.id != null) {
            return null
        }

        val auditLog = dao.new {
            this.entityId = auditLog.entityId
            this.creationDate = auditLog.creationDate
            this.changedFields = auditLog.changedFields
            this.operationType = auditLog.operationType
            this.targetTableName = auditLog.tableName
            this.user = auditLog.user
        }
        return auditLog
    }

    override fun findByTableAndId(tableName: String, entityId: Long, offsetPageable: OffsetPageable?): SizedIterable<AuditLogDao> {
        val query = table.select {
            table.targetTableName eq tableName and (table.entityId eq entityId)
        }.orderBy(*table.sortAdapter(offsetPageable))

        return if (offsetPageable != null){
            dao.wrapRows(query).limit(offsetPageable.limit, offsetPageable.offset.toLong())
        } else {
            dao.wrapRows(query)
        }
    }
}

enum class AuditLogSortEnum(override val apiValue: String): SortOrder {
    DateAsc("date.asc"){
        override val sort = Sort.by("creationDate").ascending()
    },

    DateDesc("date.desc"){
        override val sort = Sort.by("creationDate").descending()
    }
    ;

    companion object : SortOrderCompanion<AuditLogSortEnum>{
        override val logger: Logger = LoggerFactory.getLogger(SkillSortEnum::class.java)

        override val defaultSort = AuditLogSortEnum.DateAsc

        override fun forApiValue(apiValue: String): AuditLogSortEnum {
            return AuditLogSortEnum.values().find { it.apiValue == apiValue } ?: DateAsc.also {
                logger.warn("Sort with value ${apiValue} could not be found; using default ${DateAsc.apiValue} sort")
            }
        }
    }
}
