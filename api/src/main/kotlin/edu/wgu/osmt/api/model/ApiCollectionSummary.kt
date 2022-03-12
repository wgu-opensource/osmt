package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import java.time.LocalDateTime


data class ApiCollectionSummary(
    @JsonProperty val id: String?,
    @JsonProperty val uuid: String?,
    @JsonProperty val name: String?,
    @JsonProperty val libraryName: String?,
    @JsonProperty val skillCount: Int?,
    @JsonProperty val publishStatus: PublishStatus?,
    @JsonProperty val publishDate: LocalDateTime? = null,
    @JsonProperty val archiveDate: LocalDateTime? = null
) {

    companion object {
        fun fromCollection(collection: Collection, appConfig: AppConfig): ApiCollectionSummary {
            return ApiCollectionSummary(
                id = collection.canonicalUrl(appConfig.baseUrl),
                uuid = collection.uuid,
                name = collection.name,
                libraryName = collection.libraryName,
                skillCount = null,
                publishStatus = collection.publishStatus(),
                publishDate = collection.publishDate,
                archiveDate = collection.archiveDate
            )
        }

        fun fromDao(collectionDao: CollectionDao, appConfig: AppConfig): ApiCollectionSummary {
            return fromCollection(collectionDao.toModel(), appConfig)
        }

        fun fromDoc(collectionDoc: CollectionDoc): ApiCollectionSummary {
            return with(collectionDoc) {
                ApiCollectionSummary(
                    null,
                    uuid,
                    name,
                    libraryName,
                    skillCount,
                    publishStatus,
                    publishDate,
                    archiveDate,
                )
            }
        }
    }
}