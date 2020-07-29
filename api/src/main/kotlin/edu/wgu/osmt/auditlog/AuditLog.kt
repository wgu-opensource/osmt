package edu.wgu.osmt.auditlog

import com.google.gson.Gson
import edu.wgu.osmt.db.DatabaseData
import edu.wgu.osmt.richskill.RichSkillDescriptor
import org.springframework.security.oauth2.core.user.OAuth2User
import java.time.LocalDateTime
import java.time.ZoneOffset


data class AuditLog(
    val operationType: String,
    val entityType: String,
    val entityId: Long,
    val user: String,
    val changedFields: String,
    override val creationDate: LocalDateTime,
    override val id: Long?
) : DatabaseData<AuditLog>() {
    override fun withId(id: Long): AuditLog {
        return copy(id = id)
    }

    companion object {
        fun fromRichSkillDescriptorInsert(rsd: RichSkillDescriptor, user: OAuth2User): AuditLog {
            return AuditLog(
                operationType = "Insert",
                entityType = rsd.javaClass.name,
                entityId = rsd.id!!,
                user = user.name.toString(),
                changedFields = Gson().toJson(rsd),
                creationDate = LocalDateTime.now(ZoneOffset.UTC),
                id = null
            )
        }
    }
}
