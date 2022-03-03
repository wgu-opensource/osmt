package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillDescriptor
import java.time.ZoneId
import java.time.ZonedDateTime


@JsonInclude(JsonInclude.Include.ALWAYS)
class ApiCollection(
    @JsonProperty
    val id: String,

    @JsonProperty
    val uuid: String,

    @JsonProperty
    val name: String,

    @JsonProperty
    val creator: String,

    @JsonProperty
    val author: String?,

    @JsonProperty
    val status: PublishStatus,

    @JsonProperty
    @get:JsonProperty("isExternallyShared")
    val isExternallyShared: Boolean,

    @JsonProperty
    val creationDate: ZonedDateTime,

    @JsonProperty
    val updateDate: ZonedDateTime,

    @JsonProperty
    val publishDate: ZonedDateTime?,

    @JsonProperty
    val archiveDate: ZonedDateTime?,

    @JsonProperty
    val skills: List<ApiSkillSummary>,

    @JsonProperty
    val importedFrom: String? = null,

    @JsonProperty
    val libraryName: String? = null
) {
    @JsonProperty("@context")
    val context = "https://rsd.osmt.dev/context-v1.json"

    @JsonProperty
    val `type` = "RichSkillCollection"

    companion object {
        fun fromModel(collection: Collection, ss: List<RichSkillDescriptor>, appConfig: AppConfig): ApiCollection {
            return ApiCollection(
                    id=collection.canonicalUrl(appConfig.baseUrl),
                    uuid=collection.uuid,
                    name=collection.name,
                    creator=appConfig.defaultCreatorUri,
                    author=collection.author?.let { it.value },
                    status=collection.publishStatus(),
                    isExternallyShared=collection.isExternallyShared,
                    creationDate=collection.creationDate.atZone(ZoneId.of("UTC")),
                    updateDate=collection.updateDate.atZone(ZoneId.of("UTC")),
                    publishDate=collection.publishDate?.atZone(ZoneId.of("UTC")),
                    archiveDate=collection.archiveDate?.atZone(ZoneId.of("UTC")),
                    skills=ss.map { ApiSkillSummary.fromSkill(it, appConfig) },
                    importedFrom=collection.importedFrom,
                    libraryName=collection.libraryName,
            )

        }
        fun fromDao(collectionDao: CollectionDao, appConfig: AppConfig): ApiCollection {
            return fromModel(collectionDao.toModel(), collectionDao.skills.map{ it.toModel() }, appConfig)
        }
    }
}