package edu.wgu.osmt.searchhub

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.PaginationDefaults
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.GeneralApiException
import edu.wgu.osmt.api.model.*
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.OffsetPageable
import edu.wgu.osmt.elasticsearch.PaginatedLinks
import edu.wgu.osmt.searchhub.client.apis.SearchingApi
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.richskill.CreateSkillsTaskProcessor
import edu.wgu.osmt.richskill.RichSkillEsRepo
import edu.wgu.osmt.searchhub.client.apis.LibrariesApi
import edu.wgu.osmt.searchhub.client.apis.SharingApi
import edu.wgu.osmt.searchhub.client.infrastructure.ResponseType
import edu.wgu.osmt.searchhub.client.infrastructure.Success
import edu.wgu.osmt.searchhub.client.models.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.*
import kotlin.streams.toList

@Controller
@Transactional
class SearchHubController @Autowired constructor(
    val keywordEsRepo: KeywordEsRepo,
    val richSkillEsRepo: RichSkillEsRepo,
    val collectionEsRepo: CollectionEsRepo,
    val jobCodeEsRepo: JobCodeEsRepo,
    val appConfig: AppConfig,
    val librariesApi: LibrariesApi?,
    val searchingApi: SearchingApi?,
    val sharingApi: SharingApi?
) {
    val logger: Logger = LoggerFactory.getLogger(CreateSkillsTaskProcessor::class.java)

    @GetMapping(RoutePaths.SEARCH_HUB_LIBRARIES, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun getLibraries(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<List<LibrarySummary>> {
        try {
            verifySearchHubConfigured()
        } catch (e: Error) {
            throw GeneralApiException(
                e.message?.let { e.message } ?: "Search Hub is not configured correctly",
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }

        return librariesApi?.let {
            try {
                val librariesResult = it.getLibraries(size, from)
                ResponseEntity.status(200).body(librariesResult)
            }
            catch (e: Error) {
                logger.error("Search Hub API Error", e)
                throw GeneralApiException("Dependency Error: librariesApi", HttpStatus.INTERNAL_SERVER_ERROR)
            }
        } ?: throw GeneralApiException("Dependency Error: librariesApi", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping(RoutePaths.SEARCH_HUB_SEARCH_COLLECTIONS, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchCollections(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestBody request: ApiSearchRequest,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<List<ApiCollectionSearchResult>> {
        try {
            verifySearchHubConfigured()
        } catch (e: Error) {
            throw GeneralApiException(
                e.message?.let { e.message } ?: "Search Hub is not configured correctly",
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }

        if (!appConfig.allowPublicSearching && user === null) {
            throw GeneralApiException("Unauthorized", HttpStatus.UNAUTHORIZED)
        }

        return searchingApi?.let { api ->
            request.search?.let { search ->
                val response = try {
                    api.searchCollectionsWithHttpInfo(
                        convertToSearchHubSearch(search, request.libraries),
                        size,
                        from
                    )
                }
                catch (e: Error) {
                    logger.error("Search Hub API Error", e)
                    throw GeneralApiException("Dependency Error: searchingApi", HttpStatus.INTERNAL_SERVER_ERROR)
                }

                val collectionResults = if (response.responseType == ResponseType.Success) {
                    ((response as? Success<*>)?.data as List<*>)
                        .filterIsInstance<CollectionSummary>()
                        .map { convertToApiCollectionSummary(it) }
                        .map { ApiCollectionSearchResult(it) }
                } else throw GeneralApiException("Dependency Error: searchingApi", HttpStatus.INTERNAL_SERVER_ERROR)

                val responseHeaders = HttpHeaders()

                response.headers["X-Total-Count"]?.firstOrNull()?.let {
                    val totalCount = it.toInt()

                    responseHeaders.add("X-Total-Count", totalCount.toString())

                    val pageable = OffsetPageable(from, size)

                    // build up current uri with path and params
                    uriComponentsBuilder
                        .path(RoutePaths.SEARCH_HUB_SEARCH_COLLECTIONS)
                        .queryParam(RoutePaths.QueryParams.FROM, from)
                        .queryParam(RoutePaths.QueryParams.SIZE, size)

                    PaginatedLinks(
                        pageable,
                        totalCount,
                        uriComponentsBuilder
                    ).addToHeaders(responseHeaders)
                }

                ResponseEntity.status(200).headers(responseHeaders).body(collectionResults)

            } ?: throw GeneralApiException("Request did not contain search", HttpStatus.BAD_REQUEST)
        } ?: throw GeneralApiException("Dependency Error: searchingApi", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping(RoutePaths.SEARCH_HUB_SEARCH_SKILLS, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchSkills(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestBody request: ApiSearchRequest,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<List<ApiSkillSearchResult>> {
        try {
            verifySearchHubConfigured()
        } catch (e: Error) {
            throw GeneralApiException(
                e.message?.let { e.message } ?: "Search Hub is not configured correctly",
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }

        if (!appConfig.allowPublicSearching && user === null) {
            throw GeneralApiException("Unauthorized", HttpStatus.UNAUTHORIZED)
        }

        return searchingApi?.let { api ->
            request.search?.let { search ->
                val response = try {
                    api.searchSkillsWithHttpInfo(
                        convertToSearchHubSearch(search, request.libraries),
                        size,
                        from
                    )
                }
                catch (e: Error) {
                    logger.error("Search Hub API Error", e)
                    throw GeneralApiException("Dependency Error: searchingApi", HttpStatus.INTERNAL_SERVER_ERROR)
                }

                val skills = if (response.responseType == ResponseType.Success) {
                    ((response as? Success<*>)?.data as List<*>)
                        .filterIsInstance<SkillSummary>()
                        .map { convertToApiSkillSummary(it) }
                } else throw GeneralApiException("Dependency Error: searchingApi", HttpStatus.INTERNAL_SERVER_ERROR)

                // Check for local similar skills and convert to Skill Search Result
                val skillsResults = skills.parallelStream().map {
                    ApiSkillSearchResult(it, performSimilarityCheck(it))
                }.toList()

                val responseHeaders = HttpHeaders()

                response.headers["X-Total-Count"]?.firstOrNull()?.let {
                    val totalCount = it.toInt()

                    responseHeaders.add("X-Total-Count", totalCount.toString())

                    val pageable = OffsetPageable(from, size)

                    // build up current uri with path and params
                    uriComponentsBuilder
                        .path(RoutePaths.SEARCH_HUB_SEARCH_SKILLS)
                        .queryParam(RoutePaths.QueryParams.FROM, from)
                        .queryParam(RoutePaths.QueryParams.SIZE, size)

                    PaginatedLinks(
                        pageable,
                        totalCount,
                        uriComponentsBuilder
                    ).addToHeaders(responseHeaders)
                }

                ResponseEntity.status(200).headers(responseHeaders).body(skillsResults)

            } ?: throw GeneralApiException("Request did not contain search", HttpStatus.BAD_REQUEST)
        } ?: throw GeneralApiException("Dependency Error: searchingApi", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    fun submitCollections(collectionUris: List<String>): Result {
        verifySearchHubConfigured()

        return sharingApi?.submitCollections(convertToApiUrls(collectionUris))
            ?: throw GeneralApiException("Dependency Error: sharingApi", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    fun removeCollections(collectionUris: List<String>): Result {
        verifySearchHubConfigured()

        return sharingApi?.removeCollections(convertToApiUrls(collectionUris))
            ?: throw GeneralApiException("Dependency Error: sharingApi", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    fun submitSkills(skillUris: List<String>): Result {
        verifySearchHubConfigured()

        return sharingApi?.submitSkills(convertToApiUrls(skillUris))
            ?: throw GeneralApiException("Dependency Error: sharingApi", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    fun removeSkills(skillsUris: List<String>): Result {
        verifySearchHubConfigured()

        return sharingApi?.removeSkills(convertToApiUrls(skillsUris))
            ?: throw GeneralApiException("Dependency Error: sharingApi", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun verifySearchHubConfigured() {
        if (!appConfig.searchHubEnabled) {
            throw Error("Search Hub is not enabled")
        }

        if (appConfig.searchHubBaseUrl.isNullOrBlank() || appConfig.searchHubAccessToken.isNullOrBlank()) {
            throw Error("Search Hub is not configured correctly")
        }
    }

    private fun performSimilarityCheck(rsd: ApiSkillSummary): Boolean {
        return this.richSkillEsRepo.findSimilar(ApiSimilaritySearch(rsd.skillStatement)).hasSearchHits()
    }

    companion object {
        fun convertToSearchHubSearch(apiSearch: ApiSearch, libraries: List<UUID>?): Search {
            val advancedSearch: AdvancedSearch? = apiSearch.advanced?.let { apiAdvancedSearch ->
                AdvancedSearch(
                    libraries = libraries?.let { ArrayList(it) },
                    skillName = apiAdvancedSearch.skillName,
                    collectionName = apiAdvancedSearch.collectionName,
                    category = apiAdvancedSearch.category,
                    skillStatement = apiAdvancedSearch.skillStatement,
                    author = apiAdvancedSearch.author,
                    keywords = apiAdvancedSearch.keywords,
                    occupations = apiAdvancedSearch.occupations,
                    standards = apiAdvancedSearch.standards?.map { convertToSearchHubNamedReference(it) },
                    certifications = apiAdvancedSearch.certifications?.map { convertToSearchHubNamedReference(it) },
                    employers = apiAdvancedSearch.employers?.map { convertToSearchHubNamedReference(it) },
                    alignments = apiAdvancedSearch.alignments?.map { convertToSearchHubNamedReference(it) }
                )
            }

            return Search(
                query = apiSearch.query,
                uuids = apiSearch.uuids,
                advanced = advancedSearch
            )
        }

        fun convertToSearchHubNamedReference(apiNamedReference: ApiNamedReference): NamedReference {
            return NamedReference(
                id = apiNamedReference.id?.let { URI(it) },
                name = apiNamedReference.name
            )
        }

        fun convertToApiUrls(uris: List<String>): Urls {
            return Urls(uris.map {
                URI(it)
            })
        }

        fun convertToApiJobCode(jobCode: JobCode): ApiJobCode {
            return ApiJobCode(
                code = jobCode.code,
                frameworkName = jobCode.frameworkName,
                targetNode = jobCode.targetNode,
                targetNodeName = jobCode.targetNodeName
            )
        }

        fun convertToApiSkillSummary(skillSummary: SkillSummary): ApiSkillSummary {
            return ApiSkillSummary(
                id = skillSummary.id?.toString() ?: "",
                uuid = "",
                libraryName = skillSummary.library?.libraryName,
                skillName = skillSummary.skillName ?: "",
                skillStatement = skillSummary.skillStatement ?: "",
                // TODO: Hard coded status. Need to either return status from searchhub or make nullable.
                status = PublishStatus.Published,
                category = skillSummary.category,
                keywords = skillSummary.keywords?.map { it } ?: listOf(),
                occupations = skillSummary.occupations?.map { convertToApiJobCode(it) } ?: listOf(),
                publishDate = skillSummary.publishDate?.toLocalDateTime(),
                archiveDate = skillSummary.archiveDate?.toLocalDateTime(),
                importedFrom = null,
            )
        }

        fun convertToApiCollectionSummary(collectionSummary: CollectionSummary): ApiCollectionSummary {
            return ApiCollectionSummary (
                id = collectionSummary.id?.toString() ?: "",
                uuid = null,
                libraryName = collectionSummary.library?.libraryName,
                name = collectionSummary.name ?: "",
                skillCount = collectionSummary.skillCount,
                publishStatus = null,
                publishDate = collectionSummary.publishDate?.toLocalDateTime(),
                archiveDate = collectionSummary.archiveDate?.toLocalDateTime()
            )
        }

        @JsonInclude(JsonInclude.Include.ALWAYS)
        data class ApiSearchRequest(
            @JsonProperty("libraries")
            val libraries: List<UUID>? = null,

            @JsonProperty("search")
            val search: ApiSearch? = null
        )

        @JsonInclude(JsonInclude.Include.ALWAYS)
        data class ApiCollectionSearchResult(
            @JsonProperty("collection")
            val collection: ApiCollectionSummary? = null,
        )

        @JsonInclude(JsonInclude.Include.ALWAYS)
        data class ApiSkillSearchResult(
            @JsonProperty("skill")
            val skill: ApiSkillSummary? = null,

            @JsonProperty("similarToLocalSkill")
            val isSimilarToLocalSkill: Boolean? = null
        )
    }
}
