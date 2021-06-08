package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import edu.wgu.osmt.db.JobCodeLevel
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.collection.Collection


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
        val id: String? = null,
        val skillName: String? = null
) {
    companion object factory {
        fun fromKeyword(keyword: Keyword): ApiAlignment {
            return ApiAlignment(id=keyword.uri, skillName=keyword.value)
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
    val id: String? = null,
    val name: String? = null,
    val framework: String? = null,
    val level: JobCodeLevel? = null,
    val parents: List<ApiJobCode>? = null
) {
    companion object factory {
        fun fromJobCode(jobCode: JobCode, level: JobCodeLevel? = null, parents: List<ApiJobCode>? = null): ApiJobCode {
            return ApiJobCode(code=jobCode.code, name=jobCode.name, id=jobCode.url, framework=jobCode.framework, level=level, parents=parents)
        }
    }
}
