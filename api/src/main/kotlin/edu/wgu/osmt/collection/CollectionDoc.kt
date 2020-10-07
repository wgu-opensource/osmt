package edu.wgu.osmt.collection

import edu.wgu.osmt.db.PublishStatus
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field

@Document(indexName = "collection", createIndex = true)
data class CollectionDoc(
    @Field(name = "db_id")
    val id: Long,

    @Id
    val uuid: String,

    @Field
    val name: String,

    @Field
    val publishStatus: PublishStatus
) {
    companion object {
        val defaultSearchFields = setOf(CollectionDoc::name.name)
    }
}
