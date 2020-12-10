package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
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

data class ApiReferenceListUpdate(
    val add: List<ApiNamedReference>? = null,
    val remove: List<ApiNamedReference>? = null
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
