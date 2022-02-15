package edu.wgu.osmt.searchhub

import edu.wgu.osmt.PaginationDefaults
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.GeneralApiException
import edu.wgu.osmt.api.model.*
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.searchhub.client.apis.SearchingApi
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.richskill.RichSkillEsRepo
import edu.wgu.osmt.searchhub.client.apis.LibrariesApi
import edu.wgu.osmt.searchhub.client.apis.SharingApi
import edu.wgu.osmt.searchhub.client.models.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

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
        } catch (e: Exception) {
            throw GeneralApiException(
                e.message?.let { e.message } ?: "Search Hub is not configured correctly",
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }

        return librariesApi?.let {
            val librariesResult = it.getLibraries(size, from)
            ResponseEntity.status(200).body(librariesResult)
        } ?: throw GeneralApiException("Dependency Error", HttpStatus.INTERNAL_SERVER_ERROR)
    }


    @PostMapping(RoutePaths.SEARCH_HUB_SEARCH_COLLECTIONS, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchCollections(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(
            required = false,
            defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
        ) status: Array<String>,
        @RequestParam(required = false) sort: String?,
        @RequestBody apiSearch: ApiSearch,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<List<CollectionSummary>> {
        try {
            verifySearchHubConfigured()
        } catch (e: Exception) {
            throw GeneralApiException(
                e.message?.let { e.message } ?: "Search Hub is not configured correctly",
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }

        if (!appConfig.allowPublicSearching && user === null) {
            throw GeneralApiException("Unauthorized", HttpStatus.UNAUTHORIZED)
        }

        return searchingApi?.let {
            val collectionsResult = it.searchCollections(
                convertToSearchHubSearch(apiSearch),
                size,
                from
            )

            val responseHeaders = HttpHeaders()
            responseHeaders.add("X-Total-Count", "0")

            ResponseEntity.status(200).headers(responseHeaders).body(collectionsResult)
        } ?: throw GeneralApiException("Dependency Error: searchingApi", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping(RoutePaths.SEARCH_HUB_SEARCH_SKILLS, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchSkills(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(
            required = false,
            defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
        ) status: Array<String>,
        @RequestParam(required = false) sort: String?,
        @RequestParam(required = false) collectionId: String?,
        @RequestBody apiSearch: ApiSearch,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<List<SkillSummary>> {
        try {
            verifySearchHubConfigured()
        } catch (e: Exception) {
            throw GeneralApiException(
                e.message?.let { e.message } ?: "Search Hub is not configured correctly",
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }

        if (!appConfig.allowPublicSearching && user === null) {
            throw GeneralApiException("Unauthorized", HttpStatus.UNAUTHORIZED)
        }

        return searchingApi?.let {
            val skillsResult = it.searchSkills(
                convertToSearchHubSearch(apiSearch),
                size,
                from
            )

            val responseHeaders = HttpHeaders()
            responseHeaders.add("X-Total-Count", "0")

            ResponseEntity.status(200).headers(responseHeaders).body(skillsResult)
        } ?: throw GeneralApiException("Dependency Error: searchingApi", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    fun submitCollections(collectionUris: List<String>): Result {
        verifySearchHubConfigured()

        return sharingApi?.submitCollections(convertUriStringsToApiUrls(collectionUris))
            ?: throw Exception("Dependency Error: sharingApi")
    }

    fun removeCollections(collectionUris: List<String>): Result {
        verifySearchHubConfigured()

        return sharingApi?.removeCollections(convertUriStringsToApiUrls(collectionUris))
            ?: throw Exception("Dependency Error: sharingApi")
    }

    fun submitSkills(skillUris: List<String>): Result {
        verifySearchHubConfigured()

        return sharingApi?.submitSkills(convertUriStringsToApiUrls(skillUris))
            ?: throw Exception("Dependency Error: sharingApi")
    }

    fun removeSkills(skillsUris: List<String>): Result {
        verifySearchHubConfigured()

        return sharingApi?.removeSkills(convertUriStringsToApiUrls(skillsUris))
            ?: throw Exception("Dependency Error: sharingApi")
    }

    private fun verifySearchHubConfigured() {
        if (!appConfig.searchHubEnabled) {
            throw Exception("Search Hub is not enabled")
        }

        if (appConfig.searchHubBaseUrl.isNullOrBlank() || appConfig.searchHubAccessToken.isNullOrBlank()) {
            throw Exception("Search Hub is not configured correctly")
        }
    }

    companion object {
        fun convertToSearchHubSearch(apiSearch: ApiSearch): Search {
            val advancedSearch: AdvancedSearch? = apiSearch.advanced?.let { apiAdvancedSearch ->
                AdvancedSearch(
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

        fun convertUriStringsToApiUrls(uris: List<String>): Urls {
            return Urls(uris.map {
                URI(it)
            })
        }
    }
}

