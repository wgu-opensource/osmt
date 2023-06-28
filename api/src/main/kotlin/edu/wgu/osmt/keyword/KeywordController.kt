package edu.wgu.osmt.keyword

import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.model.ApiKeyword
import edu.wgu.osmt.api.model.ApiKeywordUpdate
import edu.wgu.osmt.api.model.KeywordSortEnum
import edu.wgu.osmt.api.model.SortOrder
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.elasticsearch.OffsetPageable
import edu.wgu.osmt.richskill.RichSkillEsRepo
import edu.wgu.osmt.task.RemoveItemTask
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder

@Controller
@Transactional
class KeywordController @Autowired constructor(
    val keywordRepository: KeywordRepository,
    val keywordEsRepo: KeywordEsRepo,
    val richSkillEsRepo: RichSkillEsRepo,
    val taskMessageService: TaskMessageService,
    val appConfig: AppConfig,
) {

    @GetMapping(RoutePaths.KEYWORD_LIST, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun allPaginated(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = true, defaultValue = "Category") type: String,
        @RequestParam(required = true, defaultValue = "") query: String,
        @RequestParam(required = true, defaultValue = "50") size: Int,
        @RequestParam(required = true, defaultValue = "0") from: Int,
        @RequestParam(required = true, defaultValue = "name.asc") sort: String?,
    ): HttpEntity<List<ApiKeyword>> {
        val sortEnum: SortOrder = KeywordSortEnum.forValueOrDefault(sort)
        val pageable = OffsetPageable(from, size, sortEnum.sort)
        val keywordType = KeywordTypeEnum.forApiValue(type) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        val searchResults = keywordEsRepo.searchKeywordsWithPageable(query, keywordType, pageable)
        val responseHeaders = HttpHeaders()
        responseHeaders.add("X-Total-Count", searchResults.totalHits.toString())
        
        return ResponseEntity.status(200)
            .headers(responseHeaders)
            .body(searchResults.map { ApiKeyword.fromModel(it.content) }.toList())
    }

    @GetMapping(RoutePaths.KEYWORD_DETAIL, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun keywordById(
        @PathVariable id: Long,
    ): HttpEntity<ApiKeyword> {

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(this.byId(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND))
    }

    @PostMapping(RoutePaths.KEYWORD_CREATE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun createKeyword(
        @RequestBody apiKeywordUpdate: ApiKeywordUpdate,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<ApiKeyword> {

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(keywordRepository.createFromApi(apiKeywordUpdate)?.let { ApiKeyword(it.toModel(), it.skills.count()) })
    }

    @PostMapping(RoutePaths.KEYWORD_UPDATE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun updateKeyword(
        @PathVariable id: Long,
        @RequestBody apiKeywordUpdate: ApiKeywordUpdate,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<ApiKeyword> {

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(keywordRepository.updateFromApi(
                id.toLong(),apiKeywordUpdate)
                ?.let {
                ApiKeyword(it.toModel(), it.skills.count())
                }
            )
    }

    @DeleteMapping(RoutePaths.KEYWORD_REMOVE)
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun deleteKeyword(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<TaskResult> {

        val task = RemoveItemTask(identifier = id.toString())
        taskMessageService.enqueueJob(TaskMessageService.removeKeyword, task)
        return Task.processingResponse(task)
    }

    private fun byId(
        id: Long,
    ): ApiKeyword? {
        val found = keywordRepository.findById(id)?.let { ApiKeyword(it.toModel(), it.skills.count()) }
        return found
    }

}
