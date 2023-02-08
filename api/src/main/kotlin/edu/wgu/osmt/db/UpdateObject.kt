package edu.wgu.osmt.db

import edu.wgu.osmt.collection.CollectionDao
import org.apache.commons.lang3.StringUtils
import org.jetbrains.exposed.dao.LongEntity
import java.time.LocalDateTime
import java.time.ZoneOffset

interface UpdateObject<T: LongEntity> {
    val id: Long?

    fun applyToDao(dao: T): Unit
}



interface HasPublishStatus<T: MutablePublishStatusDetails> {
    val publishStatus: PublishStatus?

    fun applyPublishStatus(dao: T): Unit {
        when (publishStatus) {
            PublishStatus.Archived -> dao.archiveDate = LocalDateTime.now(ZoneOffset.UTC)
            PublishStatus.Published -> dao.publishDate = LocalDateTime.now(ZoneOffset.UTC) // TODO this behavior won't allow "publishing" (though the date is set) if an archive date is still present see PublishStatusDetails#publishStatus
            PublishStatus.Unarchived -> dao.archiveDate = null
            PublishStatus.Deleted -> {
                if (dao.publishDate == null){
                    dao.archiveDate = LocalDateTime.now(ZoneOffset.UTC)
                }
            }
            else -> {} // draft is non-op
        }
        if(dao is CollectionDao && publishStatus != null) {
            dao.status = publishStatus as PublishStatus
            if (dao.status != PublishStatus.Workspace) {
                dao.workspaceOwner = StringUtils.EMPTY
            }
        }
    }
}
