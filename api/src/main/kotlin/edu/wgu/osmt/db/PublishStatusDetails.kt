package edu.wgu.osmt.db

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.time.LocalDateTime
import java.time.ZoneOffset

interface PublishStatusUpdate<UpdateObjectType> where UpdateObjectType : UpdateObject<*>, UpdateObjectType: HasPublishStatus {
    val archiveDate: Column<LocalDateTime?>
    val publishDate: Column<LocalDateTime?>
}

interface PublishStatusDetails{
    val publishDate: LocalDateTime?
    val archiveDate: LocalDateTime?

    fun publishStatus(): PublishStatus{
        return when {
            archiveDate != null && publishDate == null -> PublishStatus.Deleted
            archiveDate != null && archiveDate!!.isAfter(publishDate) -> PublishStatus.Archived
            publishDate != null -> PublishStatus.Published
            else -> PublishStatus.Draft
        }
    }
}
