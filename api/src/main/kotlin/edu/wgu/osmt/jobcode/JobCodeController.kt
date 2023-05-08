package edu.wgu.osmt.jobcode;

import edu.wgu.osmt.PaginationDefaults
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.model.ApiJobCode
import edu.wgu.osmt.api.model.JobCodeUpdate
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.elasticsearch.OffsetPageable
import edu.wgu.osmt.task.TaskResult
import edu.wgu.osmt.task.TaskStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.*

@Controller
@Transactional
class JobCodeController @Autowired constructor(
    val jobCodeEsRepo: JobCodeEsRepo,
    val jobCodeRepository: JobCodeRepository
) {

    @GetMapping(RoutePaths.JOB_CODE_LIST, produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("isAuthenticated()")
    fun allPaginated(
        @RequestParam query: String,
        @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
    ): HttpEntity<List<ApiJobCode>> {
        val searchResults = jobCodeEsRepo.typeAheadSearch(query, OffsetPageable(from, size, null))
        return ResponseEntity.status(200).body(searchResults.map { ApiJobCode.fromJobCode(it.content) }.toList())
    }

    @GetMapping(RoutePaths.JOB_CODE_DETAIL, produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("isAuthenticated()")
    fun byUuid(
        @PathVariable id: Int,
    ): HttpEntity<ApiJobCode> {
        val jobCode = jobCodeEsRepo.findById(id)
        return ResponseEntity.status(200).body(ApiJobCode.fromJobCode(jobCode.get()))
    }

    @PostMapping(RoutePaths.JOB_CODE_CREATE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun createJobCode(
        @RequestBody jobCodeUpdate: JobCodeUpdate
    ): HttpEntity<ApiJobCode> {
        val newJobCode = jobCodeRepository.createFromApi(jobCodeUpdate)
        return ResponseEntity.status(200).body(ApiJobCode.fromJobCode(newJobCode.toModel()))
    }

    @PutMapping(RoutePaths.JOB_CODE_UPDATE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority(appConfig.roleAdmin)")
    fun updateJobCode(
        @PathVariable id: Int,
        @RequestBody jobCodeUpdate: JobCodeUpdate
    ): HttpEntity<ApiJobCode> {
        return ResponseEntity.status(200).body(ApiJobCode(code = "1", targetNode = "target", targetNodeName = "targetNodeName", frameworkName = "frameworkName"))
    }

    @DeleteMapping(RoutePaths.JOB_CODE_REMOVE)
    @PreAuthorize("hasAuthority(appConfig.roleAdmin)")
    fun deleteJobCode(
        @PathVariable id: Int,
    ): HttpEntity<TaskResult> {
        return ResponseEntity.status(200).body(TaskResult(uuid = "uuid", contentType = "json", status = TaskStatus.Processing, apiResultPath = "path"))
    }

}