package edu.wgu.osmt.keyword

import edu.wgu.osmt.PaginationDefaults
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.model.ApiJobCode
import edu.wgu.osmt.api.model.ApiNamedReference
import edu.wgu.osmt.api.model.JobCodeUpdate
import edu.wgu.osmt.task.TaskResult
import edu.wgu.osmt.task.TaskStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@Controller
@Transactional
class NamedReferencesController @Autowired constructor(
    val keywordEsRepo: KeywordEsRepo,
    val keywordRepository: KeywordRepository
) {

    @GetMapping(RoutePaths.NAMED_REFERENCES_LIST, produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("isAuthenticated()")
    fun allPaginated(
        @RequestParam query: String,
        @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(required = true) type: String
    ): HttpEntity<List<ApiKeyword>> {
        val keywordType = KeywordTypeEnum.forApiValue(type) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        val searchResults = keywordEsRepo.typeAheadSearch(query, keywordType)
        return ResponseEntity.status(200).body(searchResults.map { ApiKeyword.fromKeyword(it.content) }.toList())
    }

    @GetMapping(RoutePaths.NAMED_REFERENCES_DETAIL, produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("isAuthenticated()")
    fun byUuid(
        @PathVariable id: Int,
    ): HttpEntity<ApiKeyword> {
        val keyword = keywordEsRepo.findById(id)
        return ResponseEntity.status(200).body(ApiKeyword.fromKeyword(keyword.get()))
    }

    @PostMapping(RoutePaths.JOB_CODE_CREATE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun createNamedReference(
        @RequestBody apiKeyword: ApiKeyword
    ): HttpEntity<ApiKeyword> {
        return ResponseEntity.status(200).body(ApiKeyword(2, "my keyword", "my value", KeywordTypeEnum.Keyword, "my framework"))
    }

    @PutMapping(RoutePaths.NAMED_REFERENCES_UPDATE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority(appConfig.roleAdmin)")
    fun updateNamedReference(
        @PathVariable id: Int,
        @RequestBody apiKeyword: ApiKeyword
    ): HttpEntity<ApiKeyword> {
        return ResponseEntity.status(200).body(ApiKeyword(134, "my name", "my value", KeywordTypeEnum.Keyword, "my framework"))
    }

    @DeleteMapping(RoutePaths.NAMED_REFERENCES_REMOVE)
    @PreAuthorize("hasAuthority(appConfig.roleAdmin)")
    fun deleteNamedReference(
        @PathVariable id: Int,
    ): HttpEntity<TaskResult> {
        return ResponseEntity.status(200).body(TaskResult(uuid = "uuid", contentType = "json", status = TaskStatus.Processing, apiResultPath = "path"))
    }

}