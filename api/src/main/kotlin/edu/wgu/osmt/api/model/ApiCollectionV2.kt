package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.richskill.RichSkillDescriptor
import java.util.*


@JsonInclude(JsonInclude.Include.ALWAYS)
class ApiCollectionV2(
    collection: Collection,
    ss: List<RichSkillDescriptor>,
    appConfig: AppConfig
) : AbstractApiCollection(collection, ss, EnumMap(edu.wgu.osmt.keyword.KeywordTypeEnum::class.java), appConfig) {

    companion object {
        fun fromDao(collectionDao: CollectionDao, appConfig: AppConfig): ApiCollectionV2 {
            return ApiCollectionV2(collectionDao.toModel(), collectionDao.skills.map{ it.toModel() }, appConfig)
        }
    }
}