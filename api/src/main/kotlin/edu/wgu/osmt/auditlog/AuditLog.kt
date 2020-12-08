package edu.wgu.osmt.auditlog

import com.google.gson.Gson
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.db.DatabaseData
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import org.jetbrains.exposed.sql.Table
import org.springframework.security.oauth2.core.user.OAuth2User
import java.time.LocalDateTime
import java.time.ZoneOffset

enum class AuditOperationType {
    Insert,
    Update,
    Delete,
    PublishStatusChange
}

data class Change(
    val fieldName: String,
    val old: String?,
    val new: String?
)

data class AuditLog(
    override val id: Long?,
    override val creationDate: LocalDateTime,
    val operationType: String,
    val tableName: String,
    val entityId: Long,
    val user: String,
    val changedFields: String
) : DatabaseData {

    companion object {

        fun fromAtomicOp(
            table: Table,
            entityId: Long,
            changes: String,
            user: String,
            opType: AuditOperationType
        ): AuditLog {
            return AuditLog(
                id = null,
                creationDate = LocalDateTime.now(ZoneOffset.UTC),
                operationType = opType.name,
                tableName = table.tableName,
                entityId = entityId,
                user = user,
                changedFields = changes
            )
        }

        fun fromAtomicOp(
            table: Table,
            entityId: Long,
            changes: String,
            user: OAuth2User,
            opType: AuditOperationType
        ): AuditLog {
            return fromAtomicOp(table, entityId, changes, user.name.toString(), opType)
        }

        fun skillInitial(dao: RichSkillDescriptorDao): List<Change> {
            return listOf(
                Change(dao::creationDate.name, null, dao.creationDate.toString()),
                Change(dao::uuid.name, null, dao.uuid),
                Change(dao::name.name, null, dao.name),
                Change(dao::statement.name, null, dao.statement),
                Change(dao::author.name, null, dao.author?.value),
                Change(dao::jobCodes.name, null, dao.jobCodes.map { it.code }.joinToString { ";" }),
                Change(dao::category.name, null, dao.category?.value),
                Change(dao::collections.name, null, dao.collections.map { it.name }.joinToString { ";" }),

                // keywords
                Change(
                    "keywords",
                    null,
                    dao.keywords.filter { it.type == KeywordTypeEnum.Keyword }.joinToString { ";" }),
                Change(
                    "standards",
                    null,
                    dao.keywords.filter { it.type == KeywordTypeEnum.Standard }.joinToString { ";" }),
                Change(
                    "certifications",
                    null,
                    dao.keywords.filter { it.type == KeywordTypeEnum.Certification }.joinToString { ";" }),
                Change(
                    "alignments",
                    null,
                    dao.keywords.filter { it.type == KeywordTypeEnum.Alignment }.joinToString { ";" }),
                Change(
                    "employers",
                    null,
                    dao.keywords.filter { it.type == KeywordTypeEnum.Employer }.joinToString { ";" })
            )
        }

        fun collectionInitial(dao: CollectionDao): List<Change> {
            return listOf(
                Change(dao::creationDate.name, null, dao.creationDate.toString()),
                Change(dao::uuid.name, null, dao.uuid),
                Change(dao::name.name, null, dao.updateDate.toString()),
                Change(dao::author.name, null, dao.author?.value),
                Change(dao::skills.name, null, dao.skills.map { it.name }.joinToString { ";" })
            )
        }
    }
}
