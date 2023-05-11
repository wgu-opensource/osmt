package edu.wgu.osmt.keyword

import edu.wgu.osmt.PaginationDefaults
import edu.wgu.osmt.RoutePaths
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
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
        @RequestParam(required = true) type: String,
        @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(required = false) sort: String?
    ): HttpEntity<List<ApiKeyword>> {
        val keywordType = KeywordTypeEnum.forApiValue(type) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        val searchResults = keywordEsRepo.typeAheadSearch("", keywordType)
        return ResponseEntity.status(200).body(searchResults.map { ApiKeyword.fromKeyword(it.content) }.toList())
    }

    @GetMapping(RoutePaths.NAMED_REFERENCES_DETAIL, produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("isAuthenticated()")
    fun byId(
        @PathVariable id: Int,
    ): HttpEntity<ApiKeyword> {
        val keyword = keywordEsRepo.findById(id)
        return ResponseEntity.status(200).body(ApiKeyword.fromKeyword(keyword.get()))
    }

    @PostMapping(RoutePaths.NAMED_REFERENCES_CREATE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun createNamedReference(
        @RequestBody keywords: List<ApiKeywordUpdate>
    ): HttpEntity<List<ApiKeyword>> {
        return ResponseEntity.status(200).body(
            keywords.map {
                ApiKeyword(id = 1, name = it.name, value = it.value, type = it.type, framework = it.framework)
            }
        )
    }

    @PostMapping(RoutePaths.NAMED_REFERENCES_UPDATE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun updateNamedReference(
        @PathVariable id: Int,
        @RequestBody apiKeyword: ApiKeywordUpdate
    ): HttpEntity<ApiKeyword> {
        return ResponseEntity.status(200).body(ApiKeyword(134, "my name", "my value", KeywordTypeEnum.Keyword, "my framework"))
    }

    @DeleteMapping(RoutePaths.NAMED_REFERENCES_REMOVE)
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun deleteNamedReference(
        @PathVariable id: Int,
    ): HttpEntity<TaskResult> {
        return ResponseEntity.status(200).body(TaskResult(uuid = "uuid", contentType = "application/json", status = TaskStatus.Processing, apiResultPath = "path"))
    }

}