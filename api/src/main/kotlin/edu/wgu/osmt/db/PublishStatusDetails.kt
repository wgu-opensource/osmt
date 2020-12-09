package edu.wgu.osmt.db

import org.jetbrains.exposed.sql.Column
import java.time.LocalDateTime

interface PublishStatusUpdate<UpdateObjectType> where UpdateObjectType : UpdateObject<*>, UpdateObjectType: HasPublishStatus<*> {
    val archiveDate: Column<LocalDateTime?>
    val publishDate: Column<LocalDateTime?>
}

interface PublishStatusDetails{
    val publishDate: LocalDateTime?
    val archiveDate: LocalDateTime?

    fun publishStatus(): PublishStatus{
        return when {
            archiveDate != null && publishDate == null -> PublishStatus.Deleted
            archiveDate != null && publishDate != null -> PublishStatus.Archived
            publishDate != null -> PublishStatus.Published
            else -> PublishStatus.Draft
        }
    }
}

interface MutablePublishStatusDetails: PublishStatusDetails{
    override var publishDate: LocalDateTime?
    override var archiveDate: LocalDateTime?
}
