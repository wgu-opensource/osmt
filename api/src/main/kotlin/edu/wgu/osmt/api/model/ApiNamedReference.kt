package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.db.JobCodeLevel
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ApiNamedReference(
    val id: String? = null,
    val name: String? = null
) {
    companion object factory {
        fun fromKeyword(keyword: Keyword): ApiNamedReference {
            return ApiNamedReference(id=keyword.uri, name=keyword.value)
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ApiAlignment(
        @get:JsonProperty("id")             // these explicit decorators are needed to help jackson
        @JsonProperty("id")
        val id: String? = null,

        @get:JsonProperty("skillName")
        @JsonProperty("skillName")
        val skillName: String? = null,

        @get:JsonProperty("isPartOf")
        @JsonProperty("isPartOf")
        val isPartOf: ApiNamedReference? = null
) {
    companion object factory {
        fun fromKeyword(keyword: Keyword): ApiAlignment {
            return fromStrings(keyword.uri, keyword.value, keyword.framework)
        }
        fun fromStrings(id: String?, skillName: String?, frameworkName: String?): ApiAlignment {
            val partOf = if (frameworkName?.isNotBlank() == true) ApiNamedReference(name=frameworkName) else null
            return ApiAlignment(id=id, skillName=skillName, isPartOf=partOf)
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ApiUuidReference(
    val uuid: String,
    val name: String
) {
    companion object factory {
        fun fromCollection(collection: Collection): ApiUuidReference {
            return ApiUuidReference(uuid=collection.uuid, name=collection.name)
        }
    }
}

data class ApiReferenceListUpdate(
    val add: List<ApiNamedReference>? = null,
    val remove: List<ApiNamedReference>? = null
)

data class ApiAlignmentListUpdate(
    val add: List<ApiAlignment>? = null,
    val remove: List<ApiAlignment>? = null
)

data class ApiStringListUpdate(
    val add: List<String>? = null,
    val remove: List<String>? = null
)

data class JobCodeV2(
    val major: String?,
    val minor: String?,
    val broad: String?,
    val detailed: String?,
    val code: String?,
    val name: String?,
    val description: String?,
    val framework: String?,
    val url: String?,
    val majorCode: String?,
    val minorCode: String?,
    val broadCode: String?,
    val detailedCode: String?,
    val jobRoleCode: String?
) {

    companion object factory {
        fun fromJobCode(jobCode: JobCode): JobCodeV2 {
            return  JobCodeV2(
                major = jobCode.major,
                minor = jobCode.minor,
                broad = jobCode.broad,
                detailed = jobCode.detailed,
                code = jobCode.code,
                name = jobCode.name,
                description = jobCode.description,
                framework = jobCode.framework,
                url = jobCode.url,
                majorCode = jobCode.majorCode,
                minorCode = jobCode.minorCode,
                broadCode = jobCode.broadCode,
                detailedCode = jobCode.detailedCode,
                jobRoleCode = jobCode.jobRoleCode
            )
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ApiJobCodeV2(
    override val id: Long? = null,
    override val code: String,
    override val targetNode: String? = null,
    override val targetNodeName: String? = null,
    override val frameworkName: String? = null,
    override val level: JobCodeLevel? = null,
    override val parents: List<ApiJobCodeV2>? = null
): ApiJobCode(id, code, targetNode, targetNodeName, frameworkName, level, parents) {
    companion object factory {

        fun fromApiJobCode(apiJobCode: ApiJobCode): ApiJobCodeV2 {
            return ApiJobCodeV2(
                code = apiJobCode.code,
                targetNode = apiJobCode.targetNode,
                targetNodeName = apiJobCode.targetNodeName,
                frameworkName = apiJobCode.frameworkName,
                level = apiJobCode.level,
                parents = apiJobCode.parents?.map { fromApiJobCode(it) }
            )
        }

        fun fromJobCode(jobCode: JobCode, level: JobCodeLevel? = null, parents: List<ApiJobCodeV2>? = null): ApiJobCodeV2 {
            return ApiJobCodeV2(
                code = jobCode.code,
                targetNodeName = jobCode.name,
                targetNode = jobCode.url,
                frameworkName = jobCode.framework,
                level = level,
                parents = parents
            )
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
open class ApiJobCode(
    open val id: Long? = null,
    open val code: String,
    open val targetNode: String? = null,
    open val targetNodeName: String? = null,
    open val frameworkName: String? = null,
    open val level: JobCodeLevel? = null,
    open val parents: List<ApiJobCode>? = null,
    val jobCodeLevelAsNumber: Int? = null,
) {
    companion object factory {
        fun fromJobCode(jobCode: JobCode, level: JobCodeLevel? = null, parents: List<ApiJobCode>? = null): ApiJobCode {
            return ApiJobCode(
                id = jobCode.id,
                code = jobCode.code,
                targetNodeName = jobCode.name,
                targetNode = jobCode.url,
                frameworkName = jobCode.framework,
                level = level,
                parents = parents,
                jobCodeLevelAsNumber = jobCode.jobCodeLevelAsNumber
            )
        }

        fun getLevelFromJobCode(jobCode: JobCode): JobCodeLevel {
            return when (jobCode.code) {
                jobCode.majorCode -> {
                    JobCodeLevel.Major
                }
                jobCode.minorCode -> {
                    JobCodeLevel.Minor
                }
                jobCode.broadCode -> {
                    JobCodeLevel.Broad
                }
                else -> {
                    JobCodeLevel.Detailed
                }
            }
        }
    }
}
