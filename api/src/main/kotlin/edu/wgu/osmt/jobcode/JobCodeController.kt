package edu.wgu.osmt.jobcode;

import edu.wgu.osmt.PaginationDefaults
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.model.*
import edu.wgu.osmt.db.JobCodeLevel
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.OffsetPageable
import edu.wgu.osmt.elasticsearch.PaginatedLinks
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDoc
import edu.wgu.osmt.richskill.RichSkillEsRepo
import edu.wgu.osmt.security.OAuthHelper
import edu.wgu.osmt.task.RemoveJobCodeTask
import edu.wgu.osmt.task.Task
import edu.wgu.osmt.task.TaskMessageService
import edu.wgu.osmt.task.TaskResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder

@Controller
@Transactional
class JobCodeController @Autowired constructor(
    val jobCodeEsRepo: JobCodeEsRepo,
    val jobCodeRepository: JobCodeRepository,
    val richSkillEsRepo: RichSkillEsRepo,
    val taskMessageService: TaskMessageService,
    val oAuthHelper: OAuthHelper,
) {

    @GetMapping(
        path = ["${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.JOB_CODE_LIST}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @PreAuthorize("isAuthenticated()")
    fun allPaginated(
        @RequestParam(required = true) size: Int,
        @RequestParam(required = true) from: Int,
        @RequestParam(required = false) sort: String?,
        @RequestParam(required = false) query: String?
    ): HttpEntity<List<ApiJobCode>> {
        val sortEnum: JobCodeSortEnum = JobCodeSortEnum.forValueOrDefault(sort)
        val searchResults = jobCodeEsRepo.typeAheadSearch(query, OffsetPageable(from, size, sortEnum.sort))
        val responseHeaders = HttpHeaders()
        responseHeaders.add("X-Total-Count", searchResults.totalHits.toString())
        return ResponseEntity.status(200).headers(responseHeaders).body(searchResults.map {
            val jobCodeLevel = ApiJobCode.getLevelFromJobCode(it.content)
            val parents = mutableListOf<JobCodeDao>()
            val majorCode = it.content.majorCode
            val minorCode = it.content.minorCode
            val broadCode = it.content.broadCode
            val detailedCode = it.content.detailedCode
            if (detailedCode != null && jobCodeLevel != JobCodeLevel.Detailed) {
                jobCodeRepository.findByCode(detailedCode)?.let { jobCodeDao -> parents.add(jobCodeDao) }
            }
            if (broadCode != null && jobCodeLevel != JobCodeLevel.Broad) {
                jobCodeRepository.findByCode(broadCode)?.let { jobCodeDao -> parents.add(jobCodeDao) }
            }
            if (minorCode != null && jobCodeLevel != JobCodeLevel.Minor) {
                jobCodeRepository.findByCode(minorCode)?.let { jobCodeDao -> parents.add(jobCodeDao) }
            }
            if (majorCode != null && jobCodeLevel != JobCodeLevel.Major) {
                jobCodeRepository.findByCode(majorCode)?.let { jobCodeDao -> parents.add(jobCodeDao) }
            }
            ApiJobCode.fromJobCode(it.content, jobCodeLevel, parents.map { it2 -> ApiJobCode.fromJobCode(it2.toModel(), ApiJobCode.getLevelFromJobCode(it2.toModel())) })
        }.toList())
    }

    @GetMapping(
        path = ["${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.JOB_CODE_DETAIL}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @PreAuthorize("isAuthenticated()")
    fun byId(
        @PathVariable id: Long,
    ): HttpEntity<ApiJobCode> {
        val jobCode = jobCodeRepository.findById(id)
        if (jobCode != null) {
            return ResponseEntity.status(200).body(ApiJobCode.fromJobCode(jobCode.toModel(),  ApiJobCode.getLevelFromJobCode(jobCode.toModel())))
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

    @PostMapping(
        path = ["${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.JOB_CODE_CREATE}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun createJobCode(
        @RequestBody jobCodes: List<JobCodeUpdate>
    ): HttpEntity<List<ApiJobCode>> {
        val newJobCodes = jobCodeRepository.createFromApi(jobCodes)
        return ResponseEntity.status(200).body(newJobCodes.map { ApiJobCode.fromJobCode(it.toModel()) }.toList())
    }

    @PostMapping(
        path = ["${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.JOB_CODE_UPDATE}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun updateJobCode(
        @PathVariable id: Int,
        @RequestBody jobCodeUpdate: JobCodeUpdate
    ): HttpEntity<ApiJobCode> {
        return ResponseEntity.status(200).body(
            ApiJobCode(
                id = id.toLong(),
                code = jobCodeUpdate.code,
                targetNode = jobCodeUpdate.targetNode,
                targetNodeName = jobCodeUpdate.targetNodeName,
                frameworkName = jobCodeUpdate.framework,
                parents = listOf()
            )
        )
    }

    @DeleteMapping(path = ["${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.JOB_CODE_REMOVE}"])
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun deleteJobCode(
        @PathVariable id: Int,
    ): HttpEntity<TaskResult> {
        val task = RemoveJobCodeTask(jobCodeId = id.toLong())
        taskMessageService.enqueueJob(TaskMessageService.removeJobCode, task)
        return Task.processingResponse(task)
    }

    @PostMapping(
        path = ["${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.JOB_CODE_SKILLS}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    fun searchJobCodeSkills (
        uriComponentsBuilder: UriComponentsBuilder,
        @PathVariable id: Long,
        @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(
            required = false,
            defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
        ) status: Array<String>,
        @RequestParam(required = false) sort: String? = null,
        @RequestBody(required = false) apiSearch: ApiSearch? = null,
        @AuthenticationPrincipal user: Jwt? = null
    ): HttpEntity<List<RichSkillDoc>> {
        val sortEnum = sort?.let{ SkillSortEnum.forApiValue(it)}

        val jobCode = jobCodeRepository.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return searchRelatedSkills(
            uriComponentsBuilder = uriComponentsBuilder,
            jobCode = jobCode,
            size = size,
            from = from,
            statusFilters = status,
            sort = sortEnum ?: SkillSortEnum.defaultSort,
            apiSearch = apiSearch ?: ApiSearch(),
            user = user
        )
    }

    private fun searchRelatedSkills (
        uriComponentsBuilder: UriComponentsBuilder,
        jobCode: JobCodeDao,
        size: Int,
        from: Int,
        statusFilters: Array<String>,
        sort: SkillSortEnum,
        apiSearch: ApiSearch,
        user: Jwt?
    ): HttpEntity<List<RichSkillDoc>> {

        val pageable = OffsetPageable(offset = from, limit = size, sort = sort.sort)
        val statuses = statusFilters.mapNotNull { PublishStatus.forApiValue(it) }.toMutableSet()

        if (user == null) {
            statuses.remove(PublishStatus.Deleted)
            statuses.remove(PublishStatus.Draft)
        }

        val search = ApiSearch (
            query = apiSearch.query,
            advanced = apiSearch.advanced,
            uuids = apiSearch.uuids,
            filtered = ApiFilteredSearch(
                jobCodes = apiSearch.filtered?.jobCodes,
            )
        )

        val countByApiSearch = richSkillEsRepo.countByApiSearch(search, statuses, pageable)
        val searchHits = richSkillEsRepo.byApiSearch(search, statuses, pageable)

        val responseHeaders = HttpHeaders()
        responseHeaders.add("X-Total-Count", countByApiSearch.toString())

        uriComponentsBuilder
            .path(RoutePaths.SEARCH_SKILLS)
            .queryParam(RoutePaths.QueryParams.FROM, from)
            .queryParam(RoutePaths.QueryParams.SIZE, size)
            .queryParam(RoutePaths.QueryParams.SORT, sort)
            .queryParam(RoutePaths.QueryParams.STATUS, statusFilters.joinToString(",").lowercase())

        PaginatedLinks(
            pageable,
            searchHits.totalHits.toInt(),
            uriComponentsBuilder
        ).addToHeaders(responseHeaders)

        return ResponseEntity.status(200).headers(responseHeaders)
            .body(searchHits.map { it.content }.toList())
    }

}
