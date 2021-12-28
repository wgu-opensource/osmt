package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillDescriptor
import java.time.ZoneId
import java.time.ZonedDateTime
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.db.JobCodeLevel
import edu.wgu.osmt.richskill.RichSkillDescriptorDao

@JsonInclude(JsonInclude.Include.ALWAYS)
class ApiSkill(private val rsd: RichSkillDescriptor, private val cs: Set<Collection>, private val appConfig: AppConfig) {

    @JsonProperty("@context")
    val context = appConfig.apiContext


    @JsonProperty
    val `type` = "RichSkillDescriptor"

    @get:JsonProperty
    val creator: String
        get() = appConfig.defaultCreatorUri

    @get:JsonProperty
    val author: String?
        get() = rsd.author?.let { it.value }

    @get:JsonProperty
    val status: PublishStatus
        get() = rsd.publishStatus()

    @get:JsonProperty
    val creationDate: ZonedDateTime
        get() = rsd.creationDate.atZone(ZoneId.of("UTC"))

    @get:JsonProperty
    val updateDate: ZonedDateTime
        get() = rsd.updateDate.atZone(ZoneId.of("UTC"))

    @get:JsonProperty
    val publishDate: ZonedDateTime?
        get() = rsd.publishDate?.atZone(ZoneId.of("UTC"))

    @get:JsonProperty
    val archiveDate: ZonedDateTime?
        get() = rsd.archiveDate?.atZone(ZoneId.of("UTC"))

    @get:JsonProperty
    val skillName: String
        get() = rsd.name

    @get:JsonProperty
    val skillStatement: String
        get() = rsd.statement

    @get:JsonProperty
    val keywords: List<String>
        get() = rsd.searchingKeywords.mapNotNull { it.value }

    @get:JsonProperty
    val category: String?
        get() = rsd.category?.value

    @get:JsonProperty
    val id: String
        get() = rsd.canonicalUrl(appConfig.baseUrl)

    @get:JsonProperty
    val uuid: String
        get() = rsd.uuid.toString()

    @get:JsonProperty
    val certifications: List<ApiNamedReference>
        get() = rsd.certifications.map { ApiNamedReference.fromKeyword(it) }

    @get:JsonProperty
    val standards: List<ApiAlignment>
        get() = rsd.standards.map { ApiAlignment.fromKeyword(it) }

    @get:JsonProperty
    val alignments: List<ApiAlignment>
        get() = rsd.alignments.map { ApiAlignment.fromKeyword(it) }

    @get:JsonProperty
    val occupations: List<ApiJobCode>
        get() {
            return rsd.jobCodes.filter { it.code.isNotBlank() }.map { jobCode ->
                val parents = listOfNotNull(
                    jobCode.major.let {jobCode.majorCode?.let { ApiJobCode(code=it, targetNodeName=jobCode.major, level=JobCodeLevel.Major) }},
                    jobCode.minor.let{jobCode.minorCode?.let { ApiJobCode(code=it, targetNodeName=jobCode.minor, level=JobCodeLevel.Minor) }},
                    jobCode.broad?.let {jobCode.broadCode?.let { ApiJobCode(code=it, targetNodeName=jobCode.broad, level=JobCodeLevel.Broad) }},
                    jobCode.detailed?.let {jobCode.detailedCode?.let { ApiJobCode(code=it, targetNodeName=jobCode.detailed, level=JobCodeLevel.Detailed) }}
                ).distinct()

                ApiJobCode.fromJobCode(jobCode, parents=parents)
            }
        }

    @get:JsonProperty
    val employers: List<ApiNamedReference>
        get() = rsd.employers.map { ApiNamedReference.fromKeyword(it) }

    @get:JsonProperty
    val collections: List<ApiUuidReference>
        get() = cs.map { ApiUuidReference.fromCollection(it) }

    companion object {
        fun fromDao(rsdDao: RichSkillDescriptorDao, appConfig: AppConfig): ApiSkill{
            return ApiSkill(rsdDao.toModel(), rsdDao.collections.map{ it.toModel() }.toSet(), appConfig)
        }
    }
}



