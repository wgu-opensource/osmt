package edu.wgu.osmt.db

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.time.LocalDateTime
import java.time.ZoneOffset

interface PublishStatusUpdate<UpdateObjectType> where UpdateObjectType : UpdateObject<*>, UpdateObjectType: HasPublishStatus {
    val archiveDate: Column<LocalDateTime?>
    val publishDate: Column<LocalDateTime?>

    fun updateBuilderApplyFromUpdateObject(
        updateBuilder: UpdateBuilder<Number>,
        updateObject: UpdateObjectType
    ){
        updateObject.publishStatus?.let{
            when (it){
                PublishStatus.Archived -> updateBuilder[archiveDate] =  LocalDateTime.now(ZoneOffset.UTC)
                PublishStatus.Published -> updateBuilder[publishDate] =  LocalDateTime.now(ZoneOffset.UTC)
                PublishStatus.Unpublished -> {} // non-op
            }
        }
    }

}

interface PublishStatusDetails{
    val publishDate: LocalDateTime?
    val archiveDate: LocalDateTime?

    fun publishStatus(): PublishStatus{
        return when {
            publishDate != null && archiveDate != null -> if (publishDate!!.isAfter(archiveDate)) PublishStatus.Published else PublishStatus.Archived
            publishDate != null -> PublishStatus.Published
            archiveDate != null -> PublishStatus.Archived
            else -> PublishStatus.Unpublished
        }
    }
}
