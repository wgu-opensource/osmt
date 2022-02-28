package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.JobCodeLevel
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillDescriptor
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import java.time.ZoneId
import java.time.ZonedDateTime

@JsonInclude(JsonInclude.Include.ALWAYS)
class ApiSkill(
    @JsonProperty("@context")
    val context: String = "",

    @JsonProperty
    val creator: String = "",

    @JsonProperty
    val author: String? = null,

    @JsonProperty
    val status: PublishStatus = PublishStatus.Draft,

    @JsonProperty
    val isExternallyShared: Boolean = false,

    @JsonProperty
    val creationDate: ZonedDateTime? = null,

    @JsonProperty
    val updateDate: ZonedDateTime? = null,

    @JsonProperty
    val publishDate: ZonedDateTime? = null,

    @JsonProperty
    val archiveDate: ZonedDateTime? = null,

    @JsonProperty
    val skillName: String = "",

    @JsonProperty
    val skillStatement: String = "",

    @JsonProperty
    val keywords: List<String> = listOf(),

    @JsonProperty
    val category: String? = null,

    @JsonProperty
    val id: String = "",

    @JsonProperty
    val uuid: String = "",

    @JsonProperty
    val certifications: List<ApiNamedReference> = listOf(),

    @JsonProperty
    val standards: List<ApiAlignment> = listOf(),

    @JsonProperty
    val alignments: List<ApiAlignment> = listOf(),

    @JsonProperty
    val occupations: List<ApiJobCode> = listOf(),

    @JsonProperty
    val employers: List<ApiNamedReference> = listOf(),

    @JsonProperty
    val collections: List<ApiUuidReference> = listOf(),

    @JsonProperty
    val importedFrom: String? = null,

    @JsonProperty
    val libraryName: String? = null
) {
    @JsonProperty
    val `type` = "RichSkillDescriptor"

    companion object {

        fun fromModel(rsd: RichSkillDescriptor, cs: Set<Collection>, appConfig: AppConfig): ApiSkill {
            val occupations = rsd.jobCodes.filter { it.code.isNotBlank() }.map { jobCode ->
                val parents = listOfNotNull(
                        jobCode.major.let {jobCode.majorCode?.let { ApiJobCode(code=it, targetNodeName=jobCode.major, level=JobCodeLevel.Major) }},
                        jobCode.minor.let{jobCode.minorCode?.let { ApiJobCode(code=it, targetNodeName=jobCode.minor, level=JobCodeLevel.Minor) }},
                        jobCode.broad?.let {jobCode.broadCode?.let { ApiJobCode(code=it, targetNodeName=jobCode.broad, level=JobCodeLevel.Broad) }},
                        jobCode.detailed?.let {jobCode.detailedCode?.let { ApiJobCode(code=it, targetNodeName=jobCode.detailed, level=JobCodeLevel.Detailed) }}
                ).distinct()
                ApiJobCode.fromJobCode(jobCode, parents=parents)
            }

            return ApiSkill(
                context = appConfig.rsdContextUrl,
                creator = appConfig.defaultCreatorUri,
                author = rsd.author?.let { it.value },
                status = rsd.publishStatus(),
                isExternallyShared = rsd.isExternallyShared,
                creationDate = rsd.creationDate.atZone(ZoneId.of("UTC")),
                updateDate = rsd.updateDate.atZone(ZoneId.of("UTC")),
                publishDate = rsd.publishDate?.atZone(ZoneId.of("UTC")),
                archiveDate = rsd.archiveDate?.atZone(ZoneId.of("UTC")),
                skillName = rsd.name,
                skillStatement = rsd.statement,
                keywords = rsd.searchingKeywords.mapNotNull { it.value },
                category = rsd.category?.value,
                id = rsd.canonicalUrl(appConfig.baseUrl),
                uuid = rsd.uuid.toString(),
                certifications = rsd.certifications.map { ApiNamedReference.fromKeyword(it) },
                standards = rsd.standards.map { ApiAlignment.fromKeyword(it) },
                alignments = rsd.alignments.map { ApiAlignment.fromKeyword(it) },
                occupations = occupations,
                employers = rsd.employers.map { ApiNamedReference.fromKeyword(it) },
                collections = cs.map { ApiUuidReference.fromCollection(it) },
                importedFrom = rsd.importedFrom,
                libraryName = rsd.libraryName
            )
        }

        fun fromDao(rsdDao: RichSkillDescriptorDao, appConfig: AppConfig): ApiSkill{
            return fromModel(rsdDao.toModel(), rsdDao.collections.map{ it.toModel() }.toSet(), appConfig)
        }
    }
}



