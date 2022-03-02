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
data class ApiImportReference(
        val canonicalUrl: String = "",
        val libraryName: String = ""
) {
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

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ApiJobCode(
    val code: String,
    val targetNode: String? = null,
    val targetNodeName: String? = null,
    val frameworkName: String? = null,
    val level: JobCodeLevel? = null,
    val parents: List<ApiJobCode>? = null
) {
    companion object factory {
        fun fromJobCode(jobCode: JobCode, level: JobCodeLevel? = null, parents: List<ApiJobCode>? = null): ApiJobCode {
            return ApiJobCode(code=jobCode.code, targetNodeName=jobCode.name, targetNode=jobCode.url, frameworkName=jobCode.framework, level=level, parents=parents)
        }
    }
}
