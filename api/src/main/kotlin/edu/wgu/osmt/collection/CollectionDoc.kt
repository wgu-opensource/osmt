package edu.wgu.osmt.collection

import edu.wgu.osmt.db.PublishStatus
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "collection", createIndex = true)
data class CollectionDoc(
    val id: Long,
    val uuid: String,
    val name: String,
    val publishStatus: PublishStatus,
    val skillIds: List<String>
) {
    companion object {
        fun fromCollection(c: Collection): CollectionDoc {
            return CollectionDoc(c.id!!, c.uuid, c.name, c.publishStatus(), c.skillUuids)
        }
    }
}
