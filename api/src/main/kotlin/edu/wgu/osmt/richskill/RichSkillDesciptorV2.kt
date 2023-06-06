package edu.wgu.osmt.richskill

import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.db.DatabaseData
import edu.wgu.osmt.db.HasUpdateDate
import edu.wgu.osmt.db.PublishStatusDetails
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.util.OsmtUtil.Companion.parseMultiValueStringFieldToSingleStringField
import java.time.LocalDateTime

data class RichSkillDescriptorV2(
        override val id: Long?,
        override val creationDate: LocalDateTime,
        override val updateDate: LocalDateTime,
        override val uuid: String,
        override val name: String,
        override val statement: String,
        override val jobCodes: List<JobCode> = listOf(),
        override val keywords: List<Keyword> = listOf(),
        val author: String,
        val category: String,
        override val archiveDate: LocalDateTime? = null,
        override val publishDate: LocalDateTime? = null,
        override val collections: List<Collection> = listOf()
) : DatabaseData, HasUpdateDate, PublishStatusDetails, RichSkillDescriptor(id, creationDate, updateDate, uuid, name, statement) {

    companion object {
        fun fromLatest(rsd: RichSkillDescriptor) : RichSkillDescriptorV2 {

            val rsdv2 = RichSkillDescriptorV2(
                    id = rsd.id,
                    creationDate = rsd.creationDate,
                    updateDate = rsd.updateDate,
                    uuid = rsd.uuid,
                    name = rsd.name,
                    statement = rsd.statement,
                    jobCodes = rsd.jobCodes,
                    keywords = rsd.keywords,
                    author = parseMultiValueStringFieldToSingleStringField(rsd.authors.mapNotNull { it.value }.toString()),
                    category = parseMultiValueStringFieldToSingleStringField(rsd.categories.mapNotNull { it.value }.toString()),
                    collections = rsd.collections
            )

            return rsdv2
        }
    }
}